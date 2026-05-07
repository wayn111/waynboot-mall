package com.wayn.common.core.service.shop.support.admin.order;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wayn.common.core.entity.shop.Member;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.mapper.shop.AdminOrderMapper;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.core.vo.MemberVO;
import com.wayn.common.core.vo.OrderGoodsVO;
import com.wayn.common.core.vo.OrderVO;
import com.wayn.common.design.strategy.pay.PayTypeEnum;
import com.wayn.common.model.request.OrderManagerReqVO;
import com.wayn.common.model.response.OrderDetailResVO;
import com.wayn.common.model.response.OrderManagerResVO;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.RefundStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端订单查询支撑服务。
 * 统一处理订单列表和详情查询，并补齐状态、退款、支付方式等展示文案。
 */
@Service
@AllArgsConstructor
public class AdminOrderQuerySupport {

    private final AdminOrderMapper adminOrderMapper;
    private final IOrderGoodsService orderGoodsService;
    private final IMemberService memberService;

    /**
     * 查询管理端订单分页并补齐展示文案。
     *
     * @param page 分页参数
     * @param order 查询条件
     * @return 订单分页结果
     */
    public IPage<OrderManagerResVO> listPage(IPage<Order> page, OrderManagerReqVO order) {
        IPage<OrderManagerResVO> result = adminOrderMapper.selectOrderListPage(page, order);
        result.getRecords().forEach(this::fillListDisplayFields);
        return result;
    }

    /**
     * 查询订单详情。
     * 该方法会聚合订单主表、订单商品和用户信息，并转换为管理端详情出参。
     *
     * @param orderId 订单 ID
     * @return 订单详情
     */
    public OrderDetailResVO detail(Long orderId) {
        Order order = adminOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ReturnCodeEnum.ERROR);
        }
        List<OrderGoods> orderGoodsList = orderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_id", orderId));
        Member member = memberService.getById(order.getUserId());
        OrderVO orderVO = BeanUtil.copyProperties(order, OrderVO.class);
        fillDetailDisplayFields(orderVO);
        OrderDetailResVO orderDetailResVO = new OrderDetailResVO();
        orderDetailResVO.setOrder(orderVO);
        orderDetailResVO.setOrderGoods(BeanUtil.copyToList(orderGoodsList, OrderGoodsVO.class));
        orderDetailResVO.setUser(BeanUtil.copyProperties(member, MemberVO.class));
        return orderDetailResVO;
    }

    /**
     * 填充列表页所需的订单状态文案。
     *
     * @param item 订单列表项
     */
    private void fillListDisplayFields(OrderManagerResVO item) {
        item.setOrderStatusMsg(OrderStatusEnum.getDescByOrderStatus(item.getOrderStatus()));
        item.setRefundStatusMsg(RefundStatusEnum.getDescByRefundStatus(item.getRefundStatus()));
        item.setRefundTypeMsg(PayTypeEnum.getDescByPayType(item.getRefundType()));
        item.setPayTypeMsg(PayTypeEnum.getDescByPayType(item.getPayType()));
    }

    /**
     * 填充详情页所需的订单状态文案。
     *
     * @param orderVO 订单详情对象
     */
    private void fillDetailDisplayFields(OrderVO orderVO) {
        orderVO.setOrderStatusMsg(OrderStatusEnum.getDescByOrderStatus(orderVO.getOrderStatus()));
        orderVO.setRefundStatusMsg(RefundStatusEnum.getDescByRefundStatus(orderVO.getRefundStatus()));
        orderVO.setRefundTypeMsg(PayTypeEnum.getDescByPayType(orderVO.getRefundType()));
        orderVO.setPayTypeMsg(PayTypeEnum.getDescByPayType(orderVO.getPayType()));
    }
}
