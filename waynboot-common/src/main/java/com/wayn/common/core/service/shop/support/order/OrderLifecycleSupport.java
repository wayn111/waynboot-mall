package com.wayn.common.core.service.shop.support.order;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.mapper.shop.OrderMapper;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 订单生命周期支撑服务。
 * 封装退款、取消、删除、确认收货等状态流转入口，保证状态判断和副作用集中维护。
 */
@Service
@AllArgsConstructor
public class OrderLifecycleSupport {

    private final OrderMapper orderMapper;
    private final IOrderGoodsService orderGoodsService;
    private final OrderValidationSupport orderValidationSupport;
    private final OrderCancellationSupport orderCancellationSupport;
    private final OrderStateTransitionSupport orderStateTransitionSupport;

    /**
     * 用户发起退款申请。
     *
     * @param orderId 订单 ID
     */
    public void refund(Long orderId) {
        Order order = orderValidationSupport.requireOrder(orderMapper.selectById(orderId));
        orderValidationSupport.ensureRefundable(order);
        orderStateTransitionSupport.validateTransition(order.getOrderStatus(), OrderStatusEnum.STATUS_REFUND,
                ReturnCodeEnum.ORDER_CANNOT_REFUND_ERROR);
        int updated = orderMapper.update(null, Wrappers.lambdaUpdate(Order.class)
                .set(Order::getOrderStatus, OrderStatusEnum.STATUS_REFUND.getStatus())
                .set(Order::getRefundStatus, 1)
                .set(Order::getUpdateTime, new Date())
                .eq(Order::getId, orderId)
                .eq(Order::getOrderStatus, order.getOrderStatus()));
        if (updated == 0) {
            throw new BusinessException(ReturnCodeEnum.ERROR);
        }
    }

    /**
     * 用户取消订单。
     *
     * @param orderId 订单 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long orderId) {
        Order order = orderValidationSupport.requireOrder(orderMapper.selectById(orderId));
        orderCancellationSupport.cancel(order.getOrderSn(), OrderStatusEnum.STATUS_CANCEL);
    }

    /**
     * 删除订单及其订单商品。
     *
     * @param orderId 订单 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long orderId) {
        Order order = orderValidationSupport.requireOrder(orderMapper.selectById(orderId));
        orderValidationSupport.ensureDeletable(order);
        int deleted = orderMapper.delete(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getId, orderId)
                .eq(Order::getOrderStatus, order.getOrderStatus()));
        if (deleted == 0) {
            throw new BusinessException(ReturnCodeEnum.ERROR);
        }
        orderGoodsService.remove(Wrappers.lambdaQuery(OrderGoods.class)
                .eq(OrderGoods::getOrderId, orderId));
    }

    /**
     * 用户确认收货。
     *
     * @param orderId 订单 ID
     */
    public void confirm(Long orderId) {
        Order order = orderValidationSupport.requireOrder(orderMapper.selectById(orderId));
        orderValidationSupport.ensureConfirmable(order);
        orderStateTransitionSupport.validateTransition(order.getOrderStatus(), OrderStatusEnum.STATUS_CONFIRM,
                ReturnCodeEnum.ORDER_CANNOT_CONFIRM_ERROR);
        int updated = orderMapper.update(null, Wrappers.lambdaUpdate(Order.class)
                .set(Order::getOrderStatus, OrderStatusEnum.STATUS_CONFIRM.getStatus())
                .set(Order::getConfirmTime, LocalDateTime.now())
                .set(Order::getUpdateTime, new Date())
                .eq(Order::getId, orderId)
                .eq(Order::getOrderStatus, order.getOrderStatus()));
        if (updated == 0) {
            throw new BusinessException(ReturnCodeEnum.ERROR);
        }
    }
}
