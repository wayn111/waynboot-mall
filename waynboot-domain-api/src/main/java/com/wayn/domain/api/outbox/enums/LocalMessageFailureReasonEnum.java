package com.wayn.domain.api.outbox.enums;

import lombok.Getter;

/**
 * 本地消息失败原因枚举。
 * 用于把 relay 异常从自由文本收敛成可统计、可告警、可运营筛选的失败分类。
 */
@Getter
public enum LocalMessageFailureReasonEnum {

    /**
     * RabbitMQ 不可用或投递失败。
     */
    RABBIT_UNAVAILABLE("RABBIT_UNAVAILABLE", "RabbitMQ 不可用"),

    /**
     * Redis 不可用，通常影响幂等标记或缓存副作用。
     */
    REDIS_UNAVAILABLE("REDIS_UNAVAILABLE", "Redis 不可用"),

    /**
     * 没有找到本地消息处理器。
     */
    HANDLER_NOT_FOUND("HANDLER_NOT_FOUND", "本地处理器缺失"),

    /**
     * 未知异常。
     */
    UNKNOWN("UNKNOWN", "未知异常");

    private final String reason;
    private final String description;

    /**
     * 构造失败原因。
     *
     * @param reason 原因编码
     * @param description 原因描述
     */
    LocalMessageFailureReasonEnum(String reason, String description) {
        this.reason = reason;
        this.description = description;
    }
}
