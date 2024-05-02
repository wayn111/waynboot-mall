package com.wayn.util.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @author: waynaqua
 * @date: 2024/4/30 10:26
 */
@Getter
public enum RefundStatusEnum {
    // 退款状态 0未退款 1申请退款 2退款成功 3退款失败
    NOT_REFUND(0, "未退款"),
    APPLY_REFUND(1, "申请退款"),
    REFUND_SUCCESS(2, "退款成功"),
    REFUND_FAIL(3, "退款失败"),

    ;

    private final Integer status;
    private final String desc;

    RefundStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static String getDescByRefundStatus(Integer status) {
        for (RefundStatusEnum orderStatusEnum : RefundStatusEnum.values()) {
            if (Objects.equals(orderStatusEnum.getStatus(), status)) {
                return orderStatusEnum.getDesc();
            }
        }
        return null;
    }
}
