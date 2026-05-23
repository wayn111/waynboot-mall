package com.wayn.common.core.enums;

/**
 * 支付回调渠道枚举。
 * 用于统一支付流水幂等键中的渠道编码，避免不同回调入口直接拼接硬编码字符串。
 */
public enum PaymentNotifyChannelEnum {

    /**
     * 微信支付回调。
     */
    WECHAT("WECHAT", "微信支付回调"),

    /**
     * 支付宝支付回调。
     */
    ALIPAY("ALIPAY", "支付宝支付回调"),

    /**
     * 易支付回调。
     */
    EPAY("EPAY", "易支付回调");

    private final String code;
    private final String description;

    /**
     * 构造支付回调渠道。
     *
     * @param code 渠道编码
     * @param description 渠道描述
     */
    PaymentNotifyChannelEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取渠道编码。
     *
     * @return 渠道编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取渠道描述。
     *
     * @return 渠道描述
     */
    public String getDescription() {
        return description;
    }
}
