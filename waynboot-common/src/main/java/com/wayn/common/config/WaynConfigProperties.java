package com.wayn.common.config;

import com.wayn.domain.api.common.WaynConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Wayn 配置绑定 Bean。
 * 负责把 application.yml 中的 wayn 配置绑定到契约层静态配置载体，避免 domain-api 自身注册 Spring 组件。
 */
@Component
@ConfigurationProperties(prefix = "wayn")
public class WaynConfigProperties extends WaynConfig {
}
