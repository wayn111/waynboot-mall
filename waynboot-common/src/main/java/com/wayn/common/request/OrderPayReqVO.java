package com.wayn.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 下单请求
 */
@Data
public class OrderPayReqVO {

    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    private String orderSn;

    /**
     * 微信公众号用户openId
     */
    private String openId;

    /**
     * 支付方式 1微信 2支付宝
     */
    @NotNull(message = "支付方式不能为空")
    private Integer payType;

    /**
     * 支付成功returnUrl
     */
    private String returnUrl;

    /**
     * 实际支付金额
     */
    private BigDecimal actualPrice;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 用户下单ip
     */
    private String clientIp;

}
