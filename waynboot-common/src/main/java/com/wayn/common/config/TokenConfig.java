package com.wayn.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "token")
public class TokenConfig {

    /**
     * 令牌自定义标识
     */
    private String header;
    /**
     * 令牌秘钥
     */
    private String secret;
    /**
     * 令牌有效期（默认30分钟）
     */
    private int expireTime;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }
}
