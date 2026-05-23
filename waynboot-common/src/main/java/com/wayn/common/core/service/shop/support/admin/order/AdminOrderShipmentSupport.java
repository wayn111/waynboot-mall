package com.wayn.common.core.service.shop.support.admin.order;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.enums.OrderStatusChangeTypeEnum;
import com.wayn.common.core.mapper.shop.AdminOrderMapper;
import com.wayn.common.core.service.shop.OrderStatusChangeCommand;
import com.wayn.common.core.service.shop.OrderStatusLogService;
import com.wayn.common.core.service.shop.support.order.OrderStateTransitionSupport;
import com.wayn.common.model.request.ShipRequestVO;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 管理端订单发货支撑服务。
 * 统一封装发货前校验和带当前状态条件的发货更新，避免重复发货覆盖订单状态。
 */
@Service
@AllArgsConstructor
public class AdminOrderShipmentSupport {

    private final AdminOrderMapper adminOrderMapper;
    private final OrderStateTransitionSupport orderStateTransitionSupport;
    private final OrderStatusLogService orderStatusLogService;

    /**
     * 执行订单发货。
     * 发货更新使用当前状态条件约束，只允许支付态订单进入发货态，避免并发下重复发货覆盖状态。
     *
     * @param shipVO 发货请求
     */
    public void ship(ShipRequestVO shipVO) {
        Long orderId = shipVO.getOrderId();
        String shipChannel = shipVO.getShipChannel();
        String shipSn = shipVO.getShipSn();
        Order order = adminOrderMapper.selectById(orderId);
        if (order == null || StringUtils.isBlank(shipChannel) || StringUtils.isBlank(shipSn)) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
        orderStateTransitionSupport.validateTransition(order.getOrderStatus(), OrderStatusEnum.STATUS_SHIP,
                ReturnCodeEnum.ORDER_CANNOT_SHIP_ERROR);
        Order update = new Order();
        update.setOrderStatus(OrderStatusEnum.STATUS_SHIP.getStatus());
        update.setShipSn(shipSn);
        update.setShipChannel(shipChannel);
        update.setShipTime(LocalDateTime.now());
        update.setUpdateTime(new Date());
        var updateWrapper = Wrappers.lambdaUpdate(Order.class)
                .eq(Order::getId, orderId);
        orderStateTransitionSupport.applyExpectedStatus(updateWrapper, OrderStatusEnum.STATUS_PAY);
        int updated = adminOrderMapper.update(update, updateWrapper);
        if (updated == 0) {
            orderStatusLogService.recordFailure(buildShipStatusLogCommand(order), "订单状态条件更新失败");
            throw new BusinessException(ReturnCodeEnum.ORDER_CANNOT_SHIP_ERROR);
        }
        orderStatusLogService.recordSuccess(buildShipStatusLogCommand(order));
    }

    /**
     * 构建发货状态日志命令。
     *
     * @param order 订单
     * @return 状态日志命令
     */
    private OrderStatusChangeCommand buildShipStatusLogCommand(Order order) {
        return OrderStatusChangeCommand.builder()
                .orderId(order.getId())
                .orderSn(order.getOrderSn())
                .sourceStatus(orderStateTransitionSupport.resolve(order.getOrderStatus()))
                .targetStatus(OrderStatusEnum.STATUS_SHIP)
                .changeType(OrderStatusChangeTypeEnum.ADMIN_SHIP)
                .operatorType("ADMIN")
                .operatorId("admin")
                .remark("管理端订单发货")
                .build();
    }
}
