package com.wayn.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "shop.alipay")
public class AlipayConfig {

    private String appId;
    private String rsaPrivateKey;
    private String alipayPublicKey;
    private String gateway;
    private String charset;
    private String format;
    private String logPath;
    private String signtype;
}
