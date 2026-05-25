package com.wayn.domain.api.trade.response;

import lombok.Data;

/**
 * 微信 JSAPI 支付前端调起参数。
 * 契约层只暴露普通字段，避免把微信支付 SDK 类型泄漏到 domain-api 和接口调用方。
 */
@Data
public class WxJsapiPayResVO {

    /**
     * 微信开放平台应用 ID。
     */
    private String appId;

    /**
     * 时间戳字符串。
     */
    private String timeStamp;

    /**
     * 随机字符串。
     */
    private String nonceStr;

    /**
     * 预支付交易会话标识，JSON 序列化时字段名仍保持 packageValue，避免 Java 关键字冲突。
     */
    private String packageValue;

    /**
     * 签名类型。
     */
    private String signType;

    /**
     * 支付签名。
     */
    private String paySign;
}
