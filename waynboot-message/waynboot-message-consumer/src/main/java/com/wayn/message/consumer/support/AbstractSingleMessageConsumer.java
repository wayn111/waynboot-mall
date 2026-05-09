package com.wayn.message.consumer.support;

import com.rabbitmq.client.Channel;
import com.wayn.data.redis.constant.RedisKeyEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

import java.io.IOException;

/**
 * 单条 MQ 消费模板。
 * 固化消息体解析、幂等判断、业务处理、ack、幂等标记和失败 nack 流程，具体消费者只实现业务回调。
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractSingleMessageConsumer {

    private final MessageConsumerSupport messageConsumerSupport;

    /**
     * 执行单条消息消费模板。
     * 重复消息直接 ack，业务处理成功后才写入消费幂等标记，失败消息保持不重新入队策略。
     *
     * @param channel RabbitMQ 通道
     * @param message RabbitMQ 消息
     * @throws IOException ack/nack 通道异常
     */
    protected final void consume(Channel channel, Message message) throws IOException {
        // 模板统一解析消息体和 msgId，避免不同消费者使用不同幂等维度造成重复消费漏洞。
        String body = messageConsumerSupport.bodyAsString(message);
        String msgId = messageConsumerSupport.resolveMessageId(message);
        log.info("{} 消费者收到消息: msgId={}, body={}", consumerName(), msgId, body);
        if (messageConsumerSupport.ackIfConsumed(channel, message, redisKeyEnum(), consumerName())) {
            // 重复消息已经由历史消费处理完成，只确认当前投递，不再调用业务接口。
            return;
        }
        try {
            // 业务回调是唯一可变步骤，ack 和幂等标记仍由模板控制，避免具体消费者漏写确认逻辑。
            handle(body);
            messageConsumerSupport.ackAndMarkConsumed(channel, message, redisKeyEnum());
        } catch (MessageConsumedMarkException e) {
            // 业务已成功且 broker 已 ack，此时不能再 nack 同一 deliveryTag，只能记录告警等待后续监控补偿。
            log.error("{} 消费幂等标记失败, msgId={}, message={}", consumerName(), msgId, e.getMessage(), e);
        } catch (Exception e) {
            // 当前队列保持非重入队策略，失败消息交给 broker 死信/丢弃配置处理，避免热点失败阻塞队列。
            messageConsumerSupport.nackWithoutRequeue(channel, message);
            log.error("{} 消费失败, msgId={}, message={}", consumerName(), msgId, e.getMessage(), e);
        }
    }

    /**
     * 返回消费者名称，用于日志定位和重复消费提示。
     *
     * @return 消费者名称
     */
    protected abstract String consumerName();

    /**
     * 返回当前消费者使用的 Redis 幂等 Key 枚举。
     *
     * @return Redis 幂等 Key 枚举
     */
    protected abstract RedisKeyEnum redisKeyEnum();

    /**
     * 执行具体业务处理。
     *
     * @param body UTF-8 消息体
     * @throws Exception 业务处理异常
     */
    protected abstract void handle(String body) throws Exception;
}
