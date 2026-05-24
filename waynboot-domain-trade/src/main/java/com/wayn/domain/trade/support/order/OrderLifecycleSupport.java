package com.wayn.domain.trade.support.order;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.entity.OrderGoods;
import com.wayn.domain.api.trade.enums.OrderStatusChangeTypeEnum;
import com.wayn.domain.api.trade.mapper.OrderMapper;
import com.wayn.domain.api.trade.service.IOrderGoodsService;
import com.wayn.domain.api.trade.service.OrderStatusChangeCommand;
import com.wayn.domain.api.trade.service.OrderStatusLogService;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.function.Consumer;

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
    private final OrderStatusLogService orderStatusLogService;

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
        executeStatusTransition(order, OrderStatusEnum.STATUS_REFUND, OrderStatusChangeTypeEnum.USER_REFUND,
                "USER", "用户申请退款", wrapper -> wrapper.set(Order::getRefundStatus, 1));
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
        OrderStatusEnum sourceStatus = orderStateTransitionSupport.resolve(order.getOrderStatus());
        var deleteWrapper = Wrappers.lambdaUpdate(Order.class)
                .eq(Order::getId, orderId);
        orderStateTransitionSupport.applyExpectedStatus(deleteWrapper, sourceStatus);
        int deleted = orderMapper.delete(deleteWrapper);
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
        executeStatusTransition(order, OrderStatusEnum.STATUS_CONFIRM, OrderStatusChangeTypeEnum.USER_CONFIRM,
                "USER", "用户确认收货", wrapper -> wrapper.set(Order::getConfirmTime, LocalDateTime.now()));
    }

    /**
     * 执行订单状态条件更新并记录状态日志。
     * 所有生命周期动作都必须带当前状态条件，避免退款、确认收货、发货等并发入口覆盖彼此结果。
     *
     * @param order 订单
     * @param targetStatus 目标状态
     * @param changeType 状态变更类型
     * @param operatorType 操作者类型
     * @param remark 备注
     * @param updateCustomizer 动作特有字段更新
     */
    private void executeStatusTransition(Order order, OrderStatusEnum targetStatus,
                                         OrderStatusChangeTypeEnum changeType, String operatorType, String remark,
                                         Consumer<LambdaUpdateWrapper<Order>> updateCustomizer) {
        OrderStatusEnum sourceStatus = orderStateTransitionSupport.resolve(order.getOrderStatus());
        var updateWrapper = Wrappers.lambdaUpdate(Order.class)
                .set(Order::getOrderStatus, targetStatus.getStatus())
                .set(Order::getUpdateTime, new Date())
                .eq(Order::getId, order.getId());
        updateCustomizer.accept(updateWrapper);
        orderStateTransitionSupport.applyExpectedStatus(updateWrapper, sourceStatus);
        OrderStatusChangeCommand command = buildStatusLogCommand(order, targetStatus, changeType, operatorType, remark);
        int updated = orderMapper.update(null, updateWrapper);
        if (updated == 0) {
            orderStatusLogService.recordFailure(command, "订单状态条件更新失败");
            throw new BusinessException(ReturnCodeEnum.ERROR);
        }
        orderStatusLogService.recordSuccess(command);
    }

    /**
     * 构建订单生命周期状态日志命令。
     *
     * @param order 订单
     * @param targetStatus 目标状态
     * @param changeType 状态变更类型
     * @param operatorType 操作者类型
     * @param remark 备注
     * @return 状态日志命令
     */
    private OrderStatusChangeCommand buildStatusLogCommand(Order order, OrderStatusEnum targetStatus,
                                                           OrderStatusChangeTypeEnum changeType,
                                                           String operatorType, String remark) {
        return OrderStatusChangeCommand.builder()
                .orderId(order.getId())
                .orderSn(order.getOrderSn())
                .sourceStatus(orderStateTransitionSupport.resolve(order.getOrderStatus()))
                .targetStatus(targetStatus)
                .changeType(changeType)
                .operatorType(operatorType)
                .operatorId(String.valueOf(order.getUserId()))
                .remark(remark)
                .build();
    }
}
