package com.wayn.domain.api.trade.enums;

/**
 * 支付流水保存结果。
 * 用于支付回调事务判断渠道流水是首次处理、同单重复通知，还是同一渠道流水绑定到不同订单的异常冲突。
 */
public enum PaymentFlowSaveResult {

    /**
     * 首次创建支付流水。
     */
    CREATED,

    /**
     * 渠道流水已存在，且归属同一订单，按幂等成功处理。
     */
    DUPLICATE_SAME_ORDER,

    /**
     * 渠道流水已存在，但归属不同订单，必须阻断当前支付回调。
     */
    DUPLICATE_CONFLICT
}
