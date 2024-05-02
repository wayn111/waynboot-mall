package com.wayn.common.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款请求
 */
@Data
public class OrderRefundReqVO {

    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    private String orderSn;

    /**
     * 退款金额
     */
    @NotNull
    @DecimalMin(value = "0", inclusive = false, message = "退款金额不能小于0")
    private BigDecimal refundMoney;

    /**
     * 退款原因
     */
    @NotBlank(message = "退款原因不能为空")
    private String refundReason;

    /**
     * 平台订单id
     */
    private String payId;
}
