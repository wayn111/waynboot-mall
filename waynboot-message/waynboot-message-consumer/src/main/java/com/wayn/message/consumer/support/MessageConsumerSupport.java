package com.wayn.message.consumer.support;

import com.rabbitmq.client.Channel;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisCache;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * MQ 消费确认支撑服务。
 * 统一封装消息体解析、幂等判断、ack、nack 和消费标记写入，消费者只保留业务调用编排。
 */
@Slf4j
@Component
@AllArgsConstructor
public class MessageConsumerSupport {

    private static final String RETURNED_MESSAGE_CORRELATION_HEADER = "spring_returned_message_correlation";

    private final RedisCache redisCache;

    /**
     * 读取 UTF-8 消息体。
     *
     * @param message RabbitMQ 消息
     * @return UTF-8 字符串消息体
     */
    public String bodyAsString(Message message) {
        return new String(message.getBody(), StandardCharsets.UTF_8);
    }

    /**
     * 解析消费幂等 ID。
     *
     * @param message RabbitMQ 消息
     * @return 稳定的消息幂等 ID
     */
    public String resolveMessageId(Message message) {
        // 生产端确认回调写入的 correlation header 优先级最高，可跨重投保持稳定。
        String msgId = message.getMessageProperties().getHeader(RETURNED_MESSAGE_CORRELATION_HEADER);
        if (StringUtils.isNotBlank(msgId)) {
            return msgId;
        }
        // 如果没有 correlation header，则退回 Rabbit messageId，兼容其他生产者投递方式。
        msgId = message.getMessageProperties().getMessageId();
        if (StringUtils.isNotBlank(msgId)) {
            return msgId;
        }
        // 最后使用 deliveryTag 兜底，保证日志和幂等 Key 不为空；该值只在当前 channel 内稳定。
        return String.valueOf(message.getMessageProperties().getDeliveryTag());
    }

    /**
     * 如果消息已消费则直接 ack。
     *
     * @param channel RabbitMQ 通道
     * @param message RabbitMQ 消息
     * @param redisKeyEnum 消费幂等 Redis Key 枚举
     * @param consumerName 消费者名称
     * @return true=重复消息已确认；false=消息待消费
     * @throws IOException ack 通道异常
     */
    public boolean ackIfConsumed(Channel channel, Message message, RedisKeyEnum redisKeyEnum,
                                 String consumerName) throws IOException {
        String msgId = resolveMessageId(message);
        if (redisCache.getCacheObject(redisKeyEnum.getKey(msgId)) == null) {
            // Redis 中没有消费标记时交给模板继续执行业务回调。
            return false;
        }
        log.info("{} 重复消息已跳过, msgId={}", consumerName, msgId);
        // 重复消息直接 ack，避免已经成功处理的消息因为重投再次进入业务链路。
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        return true;
    }

    /**
     * ack 成功消息并写入消费幂等标记。
     *
     * @param channel RabbitMQ 通道
     * @param message RabbitMQ 消息
     * @param redisKeyEnum 消费幂等 Redis Key 枚举
     * @throws IOException ack 通道异常
     */
    public void ackAndMarkConsumed(Channel channel, Message message, RedisKeyEnum redisKeyEnum) throws IOException {
        String msgId = resolveMessageId(message);
        // 保持历史顺序：先 ack broker，再写 Redis 幂等标记，避免 ack 失败但 Redis 已标记导致消息丢处理。
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        try {
            redisCache.setCacheObject(redisKeyEnum.getKey(msgId), msgId, redisKeyEnum.getExpireSecond());
        } catch (RuntimeException e) {
            throw new MessageConsumedMarkException("消息已 ack，但消费幂等标记写入失败，msgId=" + msgId, e);
        }
    }

    /**
     * nack 失败消息且不重新入队。
     *
     * @param channel RabbitMQ 通道
     * @param message RabbitMQ 消息
     * @throws IOException nack 通道异常
     */
    public void nackWithoutRequeue(Channel channel, Message message) throws IOException {
        // requeue=false 让失败消息进入 broker 死信/丢弃策略，防止不可恢复错误反复打满消费线程。
        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
    }
}
