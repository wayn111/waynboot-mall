package com.wayn.domain.api.trade.exception;

/**
 * 支付渠道异常。
 * 用于领域接口屏蔽微信、支付宝等三方 SDK 异常类型，避免 domain-api 直接依赖具体支付渠道实现。
 */
public class PaymentChannelException extends Exception {

    /**
     * 构造支付渠道异常。
     *
     * @param message 异常说明
     */
    public PaymentChannelException(String message) {
        super(message);
    }

    /**
     * 构造带原始异常的支付渠道异常。
     *
     * @param message 异常说明
     * @param cause 原始渠道异常
     */
    public PaymentChannelException(String message, Throwable cause) {
        super(message, cause);
    }
}
