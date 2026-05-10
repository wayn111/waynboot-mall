package com.wayn.common.core.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 本地消息表实体。
 * 用于在业务事务内先落本地消息，再由 relay 异步投递 MQ 或执行本地处理器，保证业务数据和异步副作用最终一致。
 */
@Data
@TableName("local_message")
@EqualsAndHashCode(callSuper = false)
public class LocalMessage extends ShopBaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -1384657114158899276L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务唯一消息键。
     */
    private String messageKey;

    /**
     * 消息主题，用于区分订单创建、延迟关单、支付后置动作等业务类型。
     */
    private String topic;

    /**
     * 业务类型，例如 ORDER、GOODS。
     */
    private String bizType;

    /**
     * 业务 ID，例如订单号、商品 ID。
     */
    private String bizId;

    /**
     * RabbitMQ 交换机名称；为空时表示本地处理器消息。
     */
    private String exchangeName;

    /**
     * RabbitMQ 路由键；为空时表示本地处理器消息。
     */
    private String routingKey;

    /**
     * JSON 消息体。
     */
    private String payload;

    /**
     * 延迟投递毫秒数，普通消息为 0。
     */
    private Integer delayMillis;

    /**
     * 消息状态：0 待投递，1 已投递，2 投递失败。
     */
    private Short status;

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
     * 成功投递或处理时间。
     */
    private Date sentTime;
}
