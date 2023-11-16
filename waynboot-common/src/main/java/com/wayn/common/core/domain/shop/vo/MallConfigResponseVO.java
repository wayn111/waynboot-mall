package com.wayn.common.core.domain.shop.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author: waynaqua
 * @date: 2023/11/13 23:08
 */
@Data
@Builder
public class MallConfigResponseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4620022749379145372L;
    /**
     * 上传路径
     */
    private String uploadDir;

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

    /**
     * 管理后台地址
     */
    private String adminUrl;
    /**
     * 商城移动端地址
     */
    private String mobileUrl;

    /**
     * 未支付订单延时取消时间
     */
    private Integer unpaidOrderCancelDelayTime;

    /**
     * 商城免运费限额
     */
    private BigDecimal freightLimit;
    /**
     * 商城运费
     */
    private BigDecimal freightPrice;
}
