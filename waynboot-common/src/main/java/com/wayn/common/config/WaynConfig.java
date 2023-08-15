package com.wayn.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
    private static String name;
    /**
     * 项目版本
     */
    private static String version;
    /**
     * 联系邮件
     */
    private static String email;

    /**
     * 管理后台地址
     */
    private static String adminUrl;
    /**
     * 商城移动端地址
     */
    private static String mobileUrl;

    /**
     * 未支付订单延时取消时间
     */
    private static Integer unpaidOrderCancelDelayTime;

    /**
     * 商城免运费限额
     */
    private static BigDecimal freightLimit;
    /**
     * 商城运费
     */
    private static BigDecimal freightPrice;

    public static String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        WaynConfig.uploadDir = uploadDir;
    }

    public static String getDownloadPath() {
        return getUploadDir() + "/download/";
    }

    public static String getAvatarPath() {
        return getUploadDir() + "/avatar/";
    }

    public static String getName() {
        return name;
    }

    public void setName(String name) {
        WaynConfig.name = name;
    }

    public static String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        WaynConfig.version = version;
    }

    public static String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        WaynConfig.email = email;
    }

    public static String getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(String adminUrl) {
        WaynConfig.adminUrl = adminUrl;
    }

    public static String getMobileUrl() {
        return mobileUrl;
    }

    public void setMobileUrl(String mobileUrl) {
        WaynConfig.mobileUrl = mobileUrl;
    }

    public static BigDecimal getFreightLimit() {
        return freightLimit;
    }

    public void setFreightLimit(BigDecimal freightLimit) {
        WaynConfig.freightLimit = freightLimit;
    }

    public static BigDecimal getFreightPrice() {
        return freightPrice;
    }

    public void setFreightPrice(BigDecimal freightPrice) {
        WaynConfig.freightPrice = freightPrice;
    }

    public void setUnpaidOrderCancelDelayTime(Integer unpaidOrderCancelDelayTime) {
        WaynConfig.unpaidOrderCancelDelayTime = unpaidOrderCancelDelayTime;
    }

    public static Integer getUnpaidOrderCancelDelayTime() {
        return unpaidOrderCancelDelayTime;
    }
}
