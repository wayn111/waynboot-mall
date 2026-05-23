package com.wayn.common.model.response;

import lombok.Data;

/**
 * 本地消息补偿指标响应。
 */
@Data
public class LocalMessageCompensationMetricResVO {

    /**
     * 当前失败消息数量。
     */
    private Long failedCount;
}
