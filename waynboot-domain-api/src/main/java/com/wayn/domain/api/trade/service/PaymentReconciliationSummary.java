package com.wayn.domain.api.trade.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 支付对账汇总结果。
 * 统计支付流水、已支付订单和差异类型数量，供后台任务或管理端接口展示。
 */
@Data
public class PaymentReconciliationSummary {

    /**
     * 扫描支付流水数量。
     */
    private int paymentFlowCount;

    /**
     * 扫描已支付订单数量。
     */
    private int paidOrderCount;

    /**
     * 金额不一致数量。
     */
    private int amountMismatchCount;

    /**
     * 订单缺失支付流水数量。
     */
    private int missingPaymentFlowCount;

    /**
     * 支付流水缺失订单数量。
     */
    private int missingOrderCount;

    /**
     * 支付流水对应订单非已支付状态数量。
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
    private List<PaymentReconciliationDifference> differences = new ArrayList<>();

    /**
     * 返回差异总数。
     *
     * @return 差异总数
     */
    public int getDifferenceCount() {
        return differences.size();
    }

    /**
     * 增加金额差异明细。
     *
     * @param difference 差异明细
     */
    public void addAmountMismatch(PaymentReconciliationDifference difference) {
        amountMismatchCount++;
        differences.add(difference);
    }

    /**
     * 增加订单缺失支付流水差异。
     *
     * @param difference 差异明细
     */
    public void addMissingPaymentFlow(PaymentReconciliationDifference difference) {
        missingPaymentFlowCount++;
        differences.add(difference);
    }

    /**
     * 增加支付流水缺失订单差异。
     *
     * @param difference 差异明细
     */
    public void addMissingOrder(PaymentReconciliationDifference difference) {
        missingOrderCount++;
        differences.add(difference);
    }

    /**
     * 增加订单状态差异。
     *
     * @param difference 差异明细
     */
    public void addOrderStatusMismatch(PaymentReconciliationDifference difference) {
        orderStatusMismatchCount++;
        differences.add(difference);
    }

    /**
     * 增加渠道账单差异。
     *
     * @param difference 差异明细
     */
    public void addChannelBillMismatch(PaymentReconciliationDifference difference) {
        channelBillMismatchCount++;
        differences.add(difference);
    }

    /**
     * 增加退款流水差异。
     *
     * @param difference 差异明细
     */
    public void addRefundFlowMismatch(PaymentReconciliationDifference difference) {
        refundFlowMismatchCount++;
        differences.add(difference);
    }

}
