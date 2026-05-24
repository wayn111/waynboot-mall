package com.wayn.domain.trade.outbox;

import com.wayn.domain.api.outbox.entity.LocalMessage;

/**
 * 本地消息处理器。
 * 用于处理不需要投递 RabbitMQ、但仍需要依赖本地消息表重试保障的业务副作用。
 */
public interface LocalMessageHandler {

    /**
     * 判断是否支持指定消息主题。
     *
     * @param topic 消息主题
     * @return true=支持处理
     */
    boolean supports(String topic);

    /**
     * 处理本地消息。
     *
     * @param message 本地消息
     */
    void handle(LocalMessage message);
}
