package com.wayn.domain.trade.outbox;

import com.wayn.domain.api.outbox.enums.LocalMessageFailureReasonEnum;

/**
 * 本地消息失败分类结果。
 * 同时返回枚举原因和归一化后的错误摘要，便于日志、指标和运营后台复用。
 */
public record LocalMessageFailureClassification(LocalMessageFailureReasonEnum reason, String summary) {

    /**
     * 获取失败原因。
     *
     * @return 失败原因
     */
    public LocalMessageFailureReasonEnum getReason() {
        return reason;
    }

    /**
     * 获取错误摘要。
     *
     * @return 错误摘要
     */
    public String getSummary() {
        return summary;
    }
}
