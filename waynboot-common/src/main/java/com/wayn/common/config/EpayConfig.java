package com.wayn.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: waynaqua
 * @date: 2024/4/30 14:46
 */
@Data
@Component
@ConfigurationProperties(prefix = "shop.epay")
public class EpayConfig {


    /**
     * 支付接口地址
     */
    private String apiurl;
    /**
     * 商户ID
     */
    private String pid;

    /**
     * 商户密钥
     */
    private String key;

    /**
     * 支付成功回调通知地址
     */
    private String notifyUrl;

}
