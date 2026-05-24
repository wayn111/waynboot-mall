package com.wayn.domain.api.trade.service;

import java.util.Date;

/**
 * 支付对账查询条件。
 * 默认只限制单次扫描数量，时间窗口可由后台任务或管理端接口传入。
 *
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @param limit 单次扫描数量
 */
public record PaymentReconciliationQuery(Date startTime, Date endTime, Integer limit) {

    private static final int DEFAULT_LIMIT = 500;

    /**
     * 创建默认支付对账查询条件。
     *
     * @return 默认查询条件
     */
    public static PaymentReconciliationQuery defaultQuery() {
        return new PaymentReconciliationQuery(null, null, DEFAULT_LIMIT);
    }

    /**
     * 返回安全查询数量。
     *
     * @return 安全查询数量
     */
    public int safeLimit() {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, DEFAULT_LIMIT);
    }
}
