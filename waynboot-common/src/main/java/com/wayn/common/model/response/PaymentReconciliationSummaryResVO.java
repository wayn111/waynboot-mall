package com.wayn.common.model.response;

import lombok.Data;

import java.util.List;

/**
 * 支付对账汇总响应 VO。
 * 管理端通过该 VO 查看本次扫描规模和各类支付对账差异数量。
 */
@Data
public class PaymentReconciliationSummaryResVO {

    /**
     * 扫描支付流水数量。
     */
    private int paymentFlowCount;

    /**
     * 扫描已支付订单数量。
     */
    private int paidOrderCount;

    /**
     * 差异总数。
     */
    private int differenceCount;

    /**
     * 金额不一致数量。
     */
    private int amountMismatchCount;

    /**
     * 订单缺少支付流水数量。
     */
    private int missingPaymentFlowCount;

    /**
     * 支付流水缺少订单数量。
     */
    private int missingOrderCount;

    /**
     * 订单状态不一致数量。
     */
    private int orderStatusMismatchCount;

    /**
     * 扫描渠道账单数量。
     */
    private int channelBillCount;

    /**
     * 扫描退款流水数量。
     */
    private int refundFlowCount;

    /**
     * 渠道账单差异数量。
     */
    private int channelBillMismatchCount;

    /**
     * 退款流水差异数量。
     */
    private int refundFlowMismatchCount;

    /**
     * 差异明细。
     */
    private List<PaymentReconciliationDifferenceResVO> differences;
}
