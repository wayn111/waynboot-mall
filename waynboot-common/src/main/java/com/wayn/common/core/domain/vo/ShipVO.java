package com.wayn.common.core.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 订单发货VO
 */
@Data
public class ShipVO {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 发货渠道
      */
    @NotBlank(message = "发货渠道不能为空")
    private String shipChannel;

    /**
     * 发货编号
     */
    @NotBlank(message = "发货编号不能为空")
    private String shipSn;
}
