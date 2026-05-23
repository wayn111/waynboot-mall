package com.wayn.common.model.response;

import lombok.Data;

import java.util.Date;

/**
 * 本地消息补偿响应 VO。
 * 用于管理端展示失败本地消息和人工重投上下文，不直接暴露数据库实体。
 */
@Data
public class LocalMessageCompensationResVO {

    /**
     * 本地消息 ID。
     */
    private Long id;

    /**
     * 业务唯一消息键。
     */
    private String messageKey;

    /**
     * 消息主题。
     */
    private String topic;

    /**
     * 业务类型。
     */
    private String bizType;

    /**
     * 业务 ID。
     */
    private String bizId;

    /**
     * 已重试次数。
     */
    private Integer retryCount;

    /**
     * 下一次可重试时间。
     */
    private Date nextRetryTime;

    /**
     * 最近一次错误信息。
     */
    private String lastError;

    /**
     * 更新时间。
     */
    private Date updateTime;
}
