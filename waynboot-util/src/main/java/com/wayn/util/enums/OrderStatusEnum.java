package com.wayn.util.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 订单流程：下单成功－》支付订单－》发货－》收货
 * 订单状态：
 * 101 订单生成，未支付；102，下单未支付用户取消；103，下单未支付超期系统自动取消
 * 201 支付完成，商家未发货；202，订单生产，已付款未发货，用户申请退款；203，管理员执行退款操作，确认退款成功；
 * 301 商家发货，用户未确认；
 * 401 用户确认收货，订单结束； 402 用户没有确认收货，但是快递反馈已收货后，超过一定时间，系统自动确认收货，订单结束。
 */
@Getter
public enum OrderStatusEnum {
    STATUS_CREATE((short) 101, "未付款"),
    STATUS_CANCEL((short) 102, "用户取消"),
    STATUS_AUTO_CANCEL((short) 103, "系统取消"),
    STATUS_PAY((short) 201, "已付款"),
    STATUS_REFUND((short) 202, "申请退款"),
    STATUS_REFUND_CONFIRM((short) 203, "退款成功"),
    STATUS_SHIP((short) 301, "已发货"),
    STATUS_CONFIRM((short) 401, "用户收货"),
    STATUS_AUTO_CONFIRM((short) 402, "系统收货"),

    ;

    private final Short status;
    private final String desc;

    OrderStatusEnum(Short status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static String getDescByOrderStatus(Short status) {
        for (OrderStatusEnum orderStatusEnum : OrderStatusEnum.values()) {
            if (Objects.equals(orderStatusEnum.getStatus(), status)) {
                return orderStatusEnum.getDesc();
            }
        }
        return null;
    }
}
