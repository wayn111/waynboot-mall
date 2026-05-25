package com.wayn.domain.api.trade.service;

/**
 * 支付对账服务接口。
 * 负责定义支付流水、渠道账单、退款流水与订单表之间的对账能力。
 */
public interface PaymentReconciliationService {

    /**
     * 执行支付对账。
     *
     * @param query 对账查询条件
     * @return 对账汇总
     */
    PaymentReconciliationSummary reconcile(PaymentReconciliationQuery query);
}
