package com.wayn.framework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wayn")
public class WaynConfig {
    /**
     * 上传路径
     */
    private static String uploadDir;

    /**
     * 项目名称
     */
    private String name;
    /**
     * 项目版本
     */
    private String version;
    /**
     * 联系邮件
     */
    private String email;

    public static String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        WaynConfig.uploadDir = uploadDir;
    }
}
