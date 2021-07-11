package com.wayn.common.core.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 订单数据VO
 */
@Data
public class OrderVO {

    /**
     * 订单ID
     */
    private String orderSn;

    /**
     * 用户购物车列表
     */
    private List<Long> cartIdArr;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 地址id
     */
    private Long addressId;

    private String message;

    /**
     * 支付方式 1微信 2支付宝
     */
    private Integer payType;
}
