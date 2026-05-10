package com.wayn.common.core.service.message;

import com.wayn.common.core.entity.message.LocalMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 本地消息 relay 支撑服务。
 * 扫描待投递消息并执行 RabbitMQ 投递或本地处理器调用，成功后标记 SENT，失败后记录重试元数据。
 */
@Slf4j
@Service
public class LocalMessageRelaySupport {

    private final LocalMessageService localMessageService;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitTemplate delayRabbitTemplate;
    private final List<LocalMessageHandler> handlers;

    /**
     * 构造本地消息 relay 支撑服务。
     *
     * @param localMessageService 本地消息服务
     * @param rabbitTemplate 普通 RabbitMQ 模板
     * @param delayRabbitTemplate 延迟 RabbitMQ 模板
     * @param handlers 本地消息处理器列表
     */
    public LocalMessageRelaySupport(LocalMessageService localMessageService,
                                    @Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate,
                                    @Qualifier("delayRabbitTemplate") RabbitTemplate delayRabbitTemplate,
                                    List<LocalMessageHandler> handlers) {
        this.localMessageService = localMessageService;
        this.rabbitTemplate = rabbitTemplate;
        this.delayRabbitTemplate = delayRabbitTemplate;
        this.handlers = handlers;
    }

    /**
     * 批量投递到期本地消息。
     * 单条消息失败不会中断本批次其他消息，避免一个坏消息阻塞整批 relay。
     *
     * @param limit 本批次最多处理数量
     */
    public void relayPendingMessages(int limit) {
        List<LocalMessage> messages = localMessageService.listPendingMessages(limit);
        for (LocalMessage message : messages) {
            try {
                dispatch(message);
                localMessageService.markSent(message.getId());
            } catch (RuntimeException e) {
                log.error("本地消息投递失败, messageId={}, messageKey={}, topic={}",
                        message.getId(), message.getMessageKey(), message.getTopic(), e);
                localMessageService.markFailed(message, e.getMessage());
            }
        }
    }

    /**
     * 分发单条本地消息。
     *
     * @param message 本地消息
     */
    private void dispatch(LocalMessage message) {
        if (isRabbitMessage(message)) {
            sendRabbitMessage(message);
            return;
        }
        LocalMessageHandler handler = handlers.stream()
                .filter(candidate -> candidate.supports(message.getTopic()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到本地消息处理器, topic=" + message.getTopic()));
        handler.handle(message);
    }

    /**
     * 判断是否为 RabbitMQ 投递消息。
     *
     * @param message 本地消息
     * @return true=RabbitMQ 消息
     */
    private boolean isRabbitMessage(LocalMessage message) {
        return StringUtils.isNotBlank(message.getExchangeName()) && StringUtils.isNotBlank(message.getRoutingKey());
    }

    /**
     * 投递 RabbitMQ 消息。
     *
     * @param message 本地消息
     */
    private void sendRabbitMessage(LocalMessage message) {
        Message rabbitMessage = buildRabbitMessage(message);
        if (message.getDelayMillis() != null && message.getDelayMillis() > 0) {
            delayRabbitTemplate.convertAndSend(message.getExchangeName(), message.getRoutingKey(),
                    rabbitMessage, messagePostProcessor -> {
                        messagePostProcessor.getMessageProperties().setDelay(message.getDelayMillis());
                        return messagePostProcessor;
                    });
            return;
        }
        rabbitTemplate.convertAndSend(message.getExchangeName(), message.getRoutingKey(),
                rabbitMessage, new CorrelationData(message.getMessageKey()));
    }

    /**
     * 构建 RabbitMQ 消息。
     *
     * @param message 本地消息
     * @return RabbitMQ 消息
     */
    private Message buildRabbitMessage(LocalMessage message) {
        return MessageBuilder.withBody(message.getPayload().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setMessageId(message.getMessageKey())
                .build();
    }
}
