package com.wayn.domain.trade.support.order;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.entity.OrderGoods;
import com.wayn.domain.api.trade.mapper.OrderMapper;
import com.wayn.domain.api.trade.service.IOrderGoodsService;
import com.wayn.domain.api.trade.response.OrderDetailVO;
import com.wayn.domain.api.trade.response.OrderGoodsVO;
import com.wayn.domain.api.trade.response.OrderListDataResVO;
import com.wayn.domain.api.trade.response.OrderListResVO;
import com.wayn.domain.api.trade.response.OrderStatusCountResVO;
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
        Map<Long, List<OrderGoods>> orderGoodsMap = listOrderGoodsMap(orders);
        List<OrderListDataResVO> dataList = buildOrderListData(orders, orderGoodsMap);

        OrderListResVO resVO = new OrderListResVO();
        resVO.setData(dataList);
        resVO.setPage(orderPage.getCurrent());
        resVO.setPages(orderPage.getPages());
        return resVO;
    }

    /**
     * 批量查询订单商品并按订单 ID 分组。
     * 列表页先拿订单主表分页，再一次性拉取商品快照，避免每个订单单独查询造成 N+1。
     *
     * @param orders 当前页订单列表
     * @return orderId 到订单商品列表的映射
     */
    private Map<Long, List<OrderGoods>> listOrderGoodsMap(List<Order> orders) {
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        if (CollectionUtils.isEmpty(orderIds)) {
            return Collections.emptyMap();
        }
        return orderGoodsService.list(Wrappers.lambdaQuery(OrderGoods.class)
                        .in(OrderGoods::getOrderId, orderIds))
                .stream()
                .collect(Collectors.groupingBy(OrderGoods::getOrderId));
    }

    /**
     * 构建订单列表返回数据。
     * 该方法只做订单主表和订单商品快照的 VO 组装，不触发数据库访问。
     *
     * @param orders 当前页订单列表
     * @param orderGoodsMap orderId 到订单商品列表的映射
     * @return 订单列表 VO
     */
    private List<OrderListDataResVO> buildOrderListData(List<Order> orders, Map<Long, List<OrderGoods>> orderGoodsMap) {
        List<OrderListDataResVO> dataList = new ArrayList<>(orders.size());
        for (Order order : orders) {
            dataList.add(buildOrderListDataItem(order,
                    orderGoodsMap.getOrDefault(order.getId(), Collections.emptyList())));
        }
        return dataList;
    }

    /**
     * 构建单个订单列表项。
     *
     * @param order 订单主表
     * @param orderGoodsList 订单商品快照
     * @return 订单列表项
     */
    private OrderListDataResVO buildOrderListDataItem(Order order, List<OrderGoods> orderGoodsList) {
        OrderListDataResVO data = new OrderListDataResVO();
        data.setId(order.getId());
        data.setOrderSn(order.getOrderSn());
        data.setActualPrice(order.getActualPrice());
        data.setHandleOption(OrderUtil.build(order));
        data.setOrderStatusText(OrderUtil.orderStatusText(order));
        data.setGoodsList(BeanUtil.copyToList(orderGoodsList, OrderGoodsVO.class));
        return data;
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
