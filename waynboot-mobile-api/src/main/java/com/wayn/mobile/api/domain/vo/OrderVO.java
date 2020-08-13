package com.wayn.mobile.api.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrderVO {

    private Long orderId;

    private List<Long> cartIdArr;

    private Long userId;

    private Long addressId;

    private String message;
}
