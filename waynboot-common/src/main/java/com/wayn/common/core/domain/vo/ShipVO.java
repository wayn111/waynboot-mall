package com.wayn.common.core.domain.vo;

import lombok.Data;

/**
 * 订单发货VO
 */
@Data
public class ShipVO {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 发货渠道
      */
    private String shipChannel;

    /**
     * 发货编号
     */
    private String shipSn;
}
