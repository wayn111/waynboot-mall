package com.wayn.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "shop.wxpay")
public class WeixinPayConfig {

    private String appId;

    private String appSecret;

    private String mchId;

    private String mchKey;

    private String notifyUrl;

    /**
     * 微信v3支付需要 apiV3 秘钥值
     */
    private String apiV3key;

    /**
     * 微信v2支付需要 p12证书文件的绝对路径或者以classpath:开头的类路径.
     */
    private String keyPath;
    /**
     * 微信v3支付需要 apiclient_key. pem证书文件的绝对路径或者以classpath:开头的类路径
     */
    private String privateKeyPath;
    /**
     * 微信v3支付需要 apiclient_cert. pem证书文件的绝对路径或者以classpath:开头的类路径.
     */
    private String privateCertPath;
}
