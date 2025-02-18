package com.wayn.message.core.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 订单数据传输对象
 */
@Data
public class OrderDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3237709318648096242L;

    /**
     * 订单编号
     */
    private String OrderSn;

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
    /**
     * 用户持有的优惠券id
     */
    private Long userCouponId;

    /**
     * 订单备注
     */
    private String message;
}
