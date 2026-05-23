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
import java.util.Collections;
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
        List<LocalMessage> messages = safePendingMessages(limit);
        for (LocalMessage message : messages) {
            if (message == null) {
                // relay 是批处理入口，空消息属于脏数据，必须跳过而不是中断整批投递。
                log.warn("跳过空本地消息投递");
                continue;
            }
            relaySingleMessage(message);
        }
    }

    /**
     * 查询待投递消息并收敛空结果。
     * relay 由定时任务触发，边界层统一把 null 结果视为空批次，避免一次异常空结果造成整轮任务 NPE。
     *
     * @param limit 本批次最多处理数量
     * @return 非空待投递消息列表
     */
    private List<LocalMessage> safePendingMessages(int limit) {
        List<LocalMessage> messages = localMessageService.listPendingMessages(limit);
        if (messages == null) {
            return Collections.emptyList();
        }
        return messages;
    }

    /**
     * 处理单条本地消息。
     * 单条消息的成功标记和失败记录集中在这里，保证 MQ 投递和本地处理器执行复用同一套状态流转。
     *
     * @param message 本地消息
     */
    private void relaySingleMessage(LocalMessage message) {
        try {
            dispatch(message);
            localMessageService.markSent(message.getId());
        } catch (Exception e) {
            log.error("本地消息投递失败, messageId={}, messageKey={}, topic={}",
                    message.getId(), message.getMessageKey(), message.getTopic(), e);
            localMessageService.markFailed(message, e.getMessage());
        }
    }

    /**
     * 分发单条本地消息。
     * 有 exchange/routingKey 的消息走 RabbitMQ；没有 MQ 路由的消息交给本地处理器，支撑支付成功后置动作等本进程补偿。
     *
     * @param message 本地消息
     */
    private void dispatch(LocalMessage message) {
        if (isRabbitMessage(message)) {
            sendRabbitMessage(message);
            return;
        }
        dispatchLocalMessage(message);
    }

    /**
     * 分发本地业务消息。
     * 本地 handler 用于不需要进入 MQ 的后置补偿动作，例如支付成功后的库存确认与展示型数据刷新。
     *
     * @param message 本地消息
     */
    private void dispatchLocalMessage(LocalMessage message) {
        findLocalHandler(message.getTopic()).handle(message);
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
        if (isDelayMessage(message)) {
            sendDelayRabbitMessage(message, rabbitMessage);
            return;
        }
        sendDirectRabbitMessage(message, rabbitMessage);
    }

    /**
     * 判断是否需要通过延迟交换机投递。
     *
     * @param message 本地消息
     * @return true=延迟消息
     */
    private boolean isDelayMessage(LocalMessage message) {
        return message.getDelayMillis() != null && message.getDelayMillis() > 0;
    }

    /**
     * 投递普通 RabbitMQ 消息。
     * 使用 messageKey 作为 CorrelationData，便于后续扩展 publisher confirm 时定位本地消息。
     *
     * @param message 本地消息
     * @param rabbitMessage RabbitMQ 消息体
     */
    private void sendDirectRabbitMessage(LocalMessage message, Message rabbitMessage) {
        rabbitTemplate.convertAndSend(message.getExchangeName(), message.getRoutingKey(),
                rabbitMessage, new CorrelationData(message.getMessageKey()));
    }

    /**
     * 投递延迟 RabbitMQ 消息。
     * 未支付关单等场景依赖延迟时间，延迟参数必须写入 MessageProperties，避免消费者立即收到消息。
     *
     * @param message 本地消息
     * @param rabbitMessage RabbitMQ 消息体
     */
    private void sendDelayRabbitMessage(LocalMessage message, Message rabbitMessage) {
        delayRabbitTemplate.convertAndSend(message.getExchangeName(), message.getRoutingKey(),
                rabbitMessage, messagePostProcessor -> {
                    messagePostProcessor.getMessageProperties().setDelay(message.getDelayMillis());
                    return messagePostProcessor;
                });
    }

    /**
     * 查找本地消息处理器。
     *
     * @param topic 消息主题
     * @return 匹配的本地处理器
     */
    private LocalMessageHandler findLocalHandler(String topic) {
        return handlers.stream()
                .filter(candidate -> candidate.supports(topic))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到本地消息处理器, topic=" + topic));
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
