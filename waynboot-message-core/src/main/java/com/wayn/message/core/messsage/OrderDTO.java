package com.wayn.message.core.messsage;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrderDTO implements Serializable {

    private static final long serialVersionUID = 3237709318648096242L;
    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户地址列表
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
}
