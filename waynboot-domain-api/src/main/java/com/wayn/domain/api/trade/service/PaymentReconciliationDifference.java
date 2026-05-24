package com.wayn.domain.api.trade.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付对账差异明细。
 * 用于承载支付流水和订单状态、订单金额之间的差异，后续可落库为对账差异表。
 */
@Data
@AllArgsConstructor
public class PaymentReconciliationDifference {

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
