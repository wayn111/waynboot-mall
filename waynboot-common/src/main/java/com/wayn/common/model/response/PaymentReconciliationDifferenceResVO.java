package com.wayn.common.model.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付对账差异响应 VO。
 * 用于展示支付流水与订单之间的状态、金额或缺失差异。
 */
@Data
public class PaymentReconciliationDifferenceResVO {

    /**
     * 差异类型。
     */
    private String differenceType;

    /**
     * 订单号。
     */
    private String orderSn;

    /**
     * 支付流水金额。
     */
    private BigDecimal flowAmount;

    /**
     * 订单实付金额。
     */
    private BigDecimal orderAmount;

    /**
     * 差异说明。
     */
    private String message;
}
