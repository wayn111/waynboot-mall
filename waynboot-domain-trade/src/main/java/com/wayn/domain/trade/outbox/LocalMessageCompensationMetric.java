package com.wayn.domain.trade.outbox;

import lombok.Data;

/**
 * 本地消息补偿指标。
 * 面向运营后台和监控采集暴露失败消息数量等轻量指标，避免直接暴露数据库实体。
 */
@Data
public class LocalMessageCompensationMetric {

    /**
     * 当前失败消息数量。
     */
    private Long failedCount;
}
