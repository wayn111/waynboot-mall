package com.wayn.domain.trade.support.order;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 订单状态流转支撑组件。
 * 集中维护订单从创建、支付、发货、退款到完成的合法流转关系，编排层只描述目标动作。
 */
@Component
public class OrderStateTransitionSupport {

    private final Map<OrderStatusEnum, Set<OrderStatusEnum>> transitions = new EnumMap<>(OrderStatusEnum.class);

    /**
     * 初始化订单状态流转规则。
     */
    public OrderStateTransitionSupport() {
        transitions.put(OrderStatusEnum.STATUS_CREATE, EnumSet.of(
                OrderStatusEnum.STATUS_PAY,
                OrderStatusEnum.STATUS_CANCEL,
                OrderStatusEnum.STATUS_AUTO_CANCEL));
        transitions.put(OrderStatusEnum.STATUS_PAY, EnumSet.of(
                OrderStatusEnum.STATUS_REFUND,
                OrderStatusEnum.STATUS_SHIP));
        transitions.put(OrderStatusEnum.STATUS_REFUND, EnumSet.of(OrderStatusEnum.STATUS_REFUND_CONFIRM));
        transitions.put(OrderStatusEnum.STATUS_SHIP, EnumSet.of(
                OrderStatusEnum.STATUS_CONFIRM,
                OrderStatusEnum.STATUS_AUTO_CONFIRM));
    }

    /**
     * 校验订单状态是否允许从当前状态切换到目标状态。
     *
     * @param currentStatus 当前状态值
     * @param targetStatus 目标状态
     * @param errorCode 非法流转时抛出的业务错误码
     */
    public void validateTransition(Short currentStatus, OrderStatusEnum targetStatus, ReturnCodeEnum errorCode) {
        validateTransition(resolve(currentStatus), targetStatus, errorCode);
    }

    /**
     * 校验订单状态是否允许从当前状态切换到目标状态。
     *
     * @param currentStatus 当前状态
     * @param targetStatus 目标状态
     * @param errorCode 非法流转时抛出的业务错误码
     */
    public void validateTransition(OrderStatusEnum currentStatus, OrderStatusEnum targetStatus,
                                   ReturnCodeEnum errorCode) {
        if (!canTransition(currentStatus, targetStatus)) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 判断订单状态是否允许从当前状态切换到目标状态。
     *
     * @param currentStatus 当前状态
     * @param targetStatus 目标状态
     * @return true=允许流转；false=拒绝流转
     */
    public boolean canTransition(OrderStatusEnum currentStatus, OrderStatusEnum targetStatus) {
        Set<OrderStatusEnum> allowedTargets = transitions.get(currentStatus);
        return allowedTargets != null && allowedTargets.contains(targetStatus);
    }

    /**
     * 判断原始订单状态值是否允许切换到目标状态。
     *
     * @param currentStatus 当前状态值
     * @param targetStatus 目标状态
     * @return true=允许流转；false=拒绝流转
     */
    public boolean canTransition(Short currentStatus, OrderStatusEnum targetStatus) {
        OrderStatusEnum sourceStatus = resolve(currentStatus);
        return sourceStatus != null && canTransition(sourceStatus, targetStatus);
    }

    /**
     * 给条件更新追加期望当前状态。
     * 所有状态变更必须带上当前状态条件，避免支付、取消、退款、发货等并发入口互相覆盖。
     *
     * @param wrapper 更新条件
     * @param expectedStatus 期望当前状态
     */
    public void applyExpectedStatus(LambdaUpdateWrapper<Order> wrapper, OrderStatusEnum expectedStatus) {
        wrapper.eq(Order::getOrderStatus, expectedStatus.getStatus());
    }

    /**
     * 按状态值解析订单状态枚举。
     *
     * @param status 订单状态值
     * @return 状态枚举；未知状态返回 null
     */
    public OrderStatusEnum resolve(Short status) {
        for (OrderStatusEnum statusEnum : OrderStatusEnum.values()) {
            if (Objects.equals(statusEnum.getStatus(), status)) {
                return statusEnum;
            }
        }
        return null;
    }
}
