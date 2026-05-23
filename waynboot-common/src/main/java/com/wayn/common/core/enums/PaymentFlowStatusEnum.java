package com.wayn.common.core.enums;

/**
 * 支付流水状态枚举。
 * 当前只落支付成功流水，后续退款、关闭、异常对账可继续扩展新的状态。
 */
public enum PaymentFlowStatusEnum {

    /**
     * 支付成功。
     */
    SUCCESS(1);

    private final Integer status;

    /**
     * 构造支付流水状态。
     *
     * @param status 状态值
     */
    PaymentFlowStatusEnum(Integer status) {
        this.status = status;
    }

    /**
     * 获取状态值。
     *
     * @return 状态值
     */
    public Integer getStatus() {
        return status;
    }
}
