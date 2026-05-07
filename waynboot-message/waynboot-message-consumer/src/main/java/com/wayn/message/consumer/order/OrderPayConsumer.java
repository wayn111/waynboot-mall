package com.wayn.message.consumer.order;


import com.rabbitmq.client.Channel;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.message.consumer.client.mobile.MobileApi;
import com.wayn.message.core.constant.MQConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.wayn.data.redis.constant.RedisKeyEnum.ORDER_CONSUMER_MAP;

/**
 * 支付订单批量消费入口。
 * 负责 MQ 层批量拉取、幂等过滤和 ack/nack，真正落单仍委托 mobile 回调接口和 common 订单服务处理。
 */
@Slf4j
@Component
public class OrderPayConsumer {

    @Resource
    private RedisCache redisCache;
    @Resource
    private MobileApi mobileApi;

    /**
     * 批量处理支付订单消息。
     * 重复消息先独立 ack，未处理消息合并调用 mobile 批量回调；批量失败时只 nack 未处理消息，避免重复消息被再次投递。
     *
     * @param messages RabbitMQ 批量消息
     * @param channel RabbitMQ 通道
     * @throws IOException ack/nack 通道异常
     */
    @RabbitListener(queues = MQConstants.ORDER_DIRECT_QUEUE, containerFactory = "orderBatchRabbitListenerContainerFactory")
    public void process(List<Message> messages, Channel channel) throws IOException {
        List<Message> pendingMessages = new ArrayList<>(messages.size());
        List<String> pendingBodies = new ArrayList<>(messages.size());
        for (Message message : messages) {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            String msgId = OrderMessageIdSupport.resolve(message);
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            log.info("OrderPayConsumer 消费者收到消息: msgId={}, body={}", msgId, body);
            if (redisCache.getCacheObject(ORDER_CONSUMER_MAP.getKey(msgId)) != null) {
                log.error("msgId: {}，消息已经被消费", msgId);
                channel.basicAck(deliveryTag, false);
                continue;
            }
            pendingMessages.add(message);
            pendingBodies.add(body);
        }
        if (pendingMessages.isEmpty()) {
            return;
        }
        try {
            mobileApi.submitOrders(pendingBodies);
            ackPendingMessages(channel, pendingMessages);
        } catch (Exception e) {
            nackPendingMessages(channel, pendingMessages);
            log.error("批量消费订单消息失败, size={}, message={}", pendingMessages.size(), e.getMessage(), e);
        }
    }

    /**
     * 确认批量中已成功处理的消息。
     * Redis 幂等标记在 ack 后写入，保持与历史单条消费者一致的确认顺序。
     *
     * @param channel RabbitMQ 通道
     * @param pendingMessages 已成功处理消息
     * @throws IOException ack 通道异常
     */
    private void ackPendingMessages(Channel channel, List<Message> pendingMessages) throws IOException {
        for (Message pendingMessage : pendingMessages) {
            String msgId = OrderMessageIdSupport.resolve(pendingMessage);
            long deliveryTag = pendingMessage.getMessageProperties().getDeliveryTag();
            channel.basicAck(deliveryTag, false);
            redisCache.setCacheObject(ORDER_CONSUMER_MAP.getKey(msgId), msgId, ORDER_CONSUMER_MAP.getExpireSecond());
        }
    }

    /**
     * 拒绝批量中未成功处理的消息。
     * 当前保持历史单条消费者的非重入队策略，失败消息交给 broker 死信或丢弃策略处理，避免热点失败消息阻塞队列。
     *
     * @param channel RabbitMQ 通道
     * @param pendingMessages 未成功处理消息
     * @throws IOException nack 通道异常
     */
    private void nackPendingMessages(Channel channel, List<Message> pendingMessages) throws IOException {
        for (Message pendingMessage : pendingMessages) {
            channel.basicNack(pendingMessage.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}
