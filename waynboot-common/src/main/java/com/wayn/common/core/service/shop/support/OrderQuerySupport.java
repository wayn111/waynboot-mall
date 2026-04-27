package com.wayn.common.core.service.shop.support;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.mapper.shop.OrderMapper;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.core.vo.OrderDetailVO;
import com.wayn.common.core.vo.OrderGoodsVO;
import com.wayn.common.model.response.OrderListDataResVO;
import com.wayn.common.model.response.OrderListResVO;
import com.wayn.common.model.response.OrderStatusCountResVO;
import com.wayn.common.util.OrderUtil;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.bean.MyBeanUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.wayn.data.redis.constant.RedisKeyEnum.ORDER_RESULT_KEY;
import static com.wayn.util.constant.SysConstants.ORDER_SUBMIT_ERROR_MSG;

/**
 * 订单查询支撑服务。
 * 聚合订单列表、详情和提交结果查询，避免查询逻辑与写流程互相耦合。
 */
@Service
@AllArgsConstructor
public class OrderQuerySupport {

    private final RedisCache redisCache;
    private final OrderMapper orderMapper;
    private final IOrderGoodsService orderGoodsService;

    /**
     * 查询用户订单列表。
     *
     * @param page 分页参数
     * @param showType 展示类型
     * @param userId 用户 ID
     * @return 订单列表结果
     */
    public OrderListResVO selectListPage(IPage<Order> page, Integer showType, Long userId) {
        List<Short> orderStatusList = OrderUtil.orderStatus(showType);
        Order query = new Order();
        query.setUserId(userId);
        IPage<Order> orderPage = orderMapper.selectOrderListPage(page, query, orderStatusList);
        List<Order> orders = orderPage.getRecords();
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, List<OrderGoods>> orderGoodsMap;
        if (CollectionUtils.isEmpty(orderIds)) {
            orderGoodsMap = Collections.emptyMap();
        } else {
            // 先批量拉取订单商品，再按订单分组，避免列表查询产生 N+1。
            orderGoodsMap = orderGoodsService.list(Wrappers.lambdaQuery(OrderGoods.class)
                            .in(OrderGoods::getOrderId, orderIds))
                    .stream()
                    .collect(Collectors.groupingBy(OrderGoods::getOrderId));
        }

        List<OrderListDataResVO> dataList = new ArrayList<>(orders.size());
        for (Order order : orders) {
            OrderListDataResVO data = new OrderListDataResVO();
            data.setId(order.getId());
            data.setOrderSn(order.getOrderSn());
            data.setActualPrice(order.getActualPrice());
            data.setHandleOption(OrderUtil.build(order));
            data.setOrderStatusText(OrderUtil.orderStatusText(order));
            List<OrderGoods> orderGoodsList = orderGoodsMap.getOrDefault(order.getId(), Collections.emptyList());
            data.setGoodsList(BeanUtil.copyToList(orderGoodsList, OrderGoodsVO.class));
            dataList.add(data);
        }

        OrderListResVO resVO = new OrderListResVO();
        resVO.setData(dataList);
        resVO.setPage(orderPage.getCurrent());
        resVO.setPages(orderPage.getPages());
        return resVO;
    }

    /**
     * 统计用户各订单状态数量。
     *
     * @param userId 用户 ID
     * @return 状态统计结果
     */
    public OrderStatusCountResVO statusCount(Long userId) {
        List<Order> orderList = orderMapper.selectList(new QueryWrapper<Order>()
                .select("order_status", "comments")
                .eq("user_id", userId));
        long unpaid = 0;
        long unship = 0;
        long unrecv = 0;
        long uncomment = 0;
        for (Order order : orderList) {
            if (OrderUtil.isCreateStatus(order)) {
                unpaid++;
            } else if (OrderUtil.isPayStatus(order)) {
                unship++;
            } else if (OrderUtil.isShipStatus(order)) {
                unrecv++;
            } else if (OrderUtil.isConfirmStatus(order) || OrderUtil.isAutoConfirmStatus(order)) {
                uncomment += Optional.ofNullable(order.getComments()).orElse(0);
            }
        }

        OrderStatusCountResVO resVO = new OrderStatusCountResVO();
        resVO.setUncomment(uncomment);
        resVO.setUnrecv(unrecv);
        resVO.setUnship(unship);
        resVO.setUnpaid(unpaid);
        return resVO;
    }

    /**
     * 根据订单号查询订单详情。
     *
     * @param orderSn 订单号
     * @return 订单详情
     */
    public OrderDetailVO getOrderDetailByOrderSn(String orderSn) {
        Order order = orderMapper.selectOne(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderSn, orderSn));
        if (order == null) {
            throw new BusinessException(ReturnCodeEnum.ORDER_NOT_EXISTS_ERROR);
        }

        OrderDetailVO orderDetailVO = new OrderDetailVO();
        MyBeanUtil.copyProperties(order, orderDetailVO);
        orderDetailVO.setOrderStatusText(OrderUtil.orderStatusText(order));
        orderDetailVO.setPayTypeText(OrderUtil.payTypeText(order));
        List<OrderGoods> orderGoodsList = orderGoodsService.list(Wrappers.lambdaQuery(OrderGoods.class)
                .eq(OrderGoods::getOrderId, order.getId()));
        orderDetailVO.setOrderGoodsVOList(BeanUtil.copyToList(orderGoodsList, OrderGoodsVO.class));
        return orderDetailVO;
    }

    /**
     * 查询异步下单结果。
     *
     * @param orderSn 订单号
     * @return 下单结果文本
     */
    public String searchResult(String orderSn) {
        // 下单结果由异步消费端写入 Redis，查询端只负责读取和兜底返回。
        String value = redisCache.getCacheObject(ORDER_RESULT_KEY.getKey(orderSn));
        return value == null ? ORDER_SUBMIT_ERROR_MSG : value;
    }
}
