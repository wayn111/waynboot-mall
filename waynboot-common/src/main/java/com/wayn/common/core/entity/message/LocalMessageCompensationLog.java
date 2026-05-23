package com.wayn.common.core.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 本地消息补偿日志实体。
 * 记录自动重试失败、进入死信和人工重投动作，避免只依赖 local_message.last_error 丢失历史排查信息。
 */
@Data
@TableName("local_message_compensation_log")
@EqualsAndHashCode(callSuper = false)
public class LocalMessageCompensationLog extends ShopBaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 3320183794906040466L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 本地消息 ID。
     */
    private Long messageId;

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
     * 动作类型：FAILURE、DEAD_LETTER、MANUAL_RETRY。
     */
    private String actionType;

    /**
     * 失败原因分类。
     */
    private String failureReason;

    /**
     * 当前重试次数。
     */
    private Integer retryCount;

    /**
     * 是否进入死信。
     */
    private Boolean deadLetter;

    /**
     * 操作者，自动任务默认为 system。
     */
    private String operator;

    /**
     * 日志备注。
     */
    private String remark;
}
