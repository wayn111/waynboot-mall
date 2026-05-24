package com.wayn.domain.api.trade.enums;

import lombok.Getter;

/**
 * 订单状态变更类型。
 * 统一沉淀状态日志来源，避免支付、取消、退款、发货链路继续散落硬编码字符串。
 */
@Getter
public enum OrderStatusChangeTypeEnum {

    PAY_CALLBACK("PAY_CALLBACK", "支付回调"),
    USER_CANCEL("USER_CANCEL", "用户取消"),
    AUTO_CANCEL("AUTO_CANCEL", "系统自动取消"),
    USER_REFUND("USER_REFUND", "用户申请退款"),
    ADMIN_REFUND("ADMIN_REFUND", "管理端退款确认"),
    ADMIN_SHIP("ADMIN_SHIP", "管理端发货"),
    USER_CONFIRM("USER_CONFIRM", "用户确认收货");

    private final String code;
    private final String description;

    /**
     * 创建订单状态变更类型。
     *
     * @param code 类型编码
     * @param description 类型说明
     */
    OrderStatusChangeTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
