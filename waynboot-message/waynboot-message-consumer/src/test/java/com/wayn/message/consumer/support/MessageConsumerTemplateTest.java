package com.wayn.message.consumer.support;

import com.rabbitmq.client.Channel;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisCache;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.wayn.data.redis.constant.RedisKeyEnum.EMAIL_CONSUMER_MAP;
import static com.wayn.data.redis.constant.RedisKeyEnum.ORDER_CONSUMER_MAP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

class MessageConsumerTemplateTest {

    /**
     * 验证单条消费模板遇到重复消息时只 ack，不再调用业务处理。
     */
    @Test
    void singleTemplateAcksDuplicateWithoutHandlingBody() throws Exception {
        RedisCache redisCache = mock(RedisCache.class);
        Channel channel = mock(Channel.class);
        MessageConsumerSupport support = new MessageConsumerSupport(redisCache);
        java.util.ArrayList<String> handledBodies = new java.util.ArrayList<>();
        TestSingleConsumer consumer = new TestSingleConsumer(support, EMAIL_CONSUMER_MAP, handledBodies);
        Message message = buildMessage(1L, "mail-1", "duplicate");
        when(redisCache.getCacheObject(EMAIL_CONSUMER_MAP.getKey("mail-1"))).thenReturn("mail-1");

        consumer.process(channel, message);

        verify(channel).basicAck(1L, false);
        assertEquals(List.of(), handledBodies);
    }

    /**
     * 验证单条消费模板处理成功后 ack 并写入幂等标记。
     */
    @Test
    void singleTemplateHandlesPendingBodyAndMarksItConsumed() throws Exception {
        RedisCache redisCache = mock(RedisCache.class);
        Channel channel = mock(Channel.class);
        MessageConsumerSupport support = new MessageConsumerSupport(redisCache);
        java.util.ArrayList<String> handledBodies = new java.util.ArrayList<>();
        TestSingleConsumer consumer = new TestSingleConsumer(support, ORDER_CONSUMER_MAP, handledBodies);
        Message pendingMessage = buildMessage(2L, "order-2", "pending");
        when(redisCache.getCacheObject(ORDER_CONSUMER_MAP.getKey("order-2"))).thenReturn(null);

        consumer.process(channel, pendingMessage);

        verify(channel).basicAck(2L, false);
        verify(redisCache).setCacheObject(ORDER_CONSUMER_MAP.getKey("order-2"),
                "order-2", ORDER_CONSUMER_MAP.getExpireSecond());
        assertEquals(List.of("pending"), handledBodies);
    }

    /**
     * 验证业务已处理且 ack 成功后，如果 Redis 幂等标记写入失败，模板不能再对同一投递执行 nack。
     */
    @Test
    void singleTemplateDoesNotNackWhenMarkConsumedFailsAfterAck() throws Exception {
        RedisCache redisCache = mock(RedisCache.class);
        Channel channel = mock(Channel.class);
        MessageConsumerSupport support = new MessageConsumerSupport(redisCache);
        java.util.ArrayList<String> handledBodies = new java.util.ArrayList<>();
        TestSingleConsumer consumer = new TestSingleConsumer(support, ORDER_CONSUMER_MAP, handledBodies);
        Message pendingMessage = buildMessage(3L, "order-3", "pending");
        when(redisCache.getCacheObject(ORDER_CONSUMER_MAP.getKey("order-3"))).thenReturn(null);
        doThrow(new RuntimeException("redis down")).when(redisCache)
                .setCacheObject(ORDER_CONSUMER_MAP.getKey("order-3"),
                        "order-3", ORDER_CONSUMER_MAP.getExpireSecond());

        consumer.process(channel, pendingMessage);

        verify(channel).basicAck(3L, false);
        verify(channel, never()).basicNack(3L, false, false);
        assertEquals(List.of("pending"), handledBodies);
    }

    /**
     * 构建测试用 RabbitMQ 消息。
     *
     * @param deliveryTag 消息投递标签
     * @param msgId 消息幂等 ID
     * @param body 消息体
     * @return RabbitMQ 消息
     */
    private Message buildMessage(long deliveryTag, String msgId, String body) {
        return MessageBuilder.withBody(body.getBytes(StandardCharsets.UTF_8))
                .setHeader("spring_returned_message_correlation", msgId)
                .setDeliveryTag(deliveryTag)
                .setMessageId(msgId)
                .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                .build();
    }

    /**
     * 测试用单条消费模板实现。
     */
    private static final class TestSingleConsumer extends AbstractSingleMessageConsumer {

        private final RedisKeyEnum redisKeyEnum;
        private final List<String> handledBodies;

        /**
         * 构造单条消费模板测试对象。
         *
         * @param messageConsumerSupport MQ 消费支撑服务
         * @param redisKeyEnum 幂等 Key 枚举
         * @param handledBodies 已处理消息体
         */
        private TestSingleConsumer(MessageConsumerSupport messageConsumerSupport, RedisKeyEnum redisKeyEnum,
                                   List<String> handledBodies) {
            super(messageConsumerSupport);
            this.redisKeyEnum = redisKeyEnum;
            this.handledBodies = handledBodies;
        }

        /**
         * 暴露模板入口供测试调用。
         *
         * @param channel RabbitMQ 通道
         * @param message RabbitMQ 消息
         * @throws Exception 消费异常
         */
        private void process(Channel channel, Message message) throws Exception {
            consume(channel, message);
        }

        /**
         * 返回消费者名称。
         *
         * @return 消费者名称
         */
        @Override
        protected String consumerName() {
            return "TestSingleConsumer";
        }

        /**
         * 返回消费幂等 Key。
         *
         * @return 幂等 Key 枚举
         */
        @Override
        protected RedisKeyEnum redisKeyEnum() {
            return redisKeyEnum;
        }

        /**
         * 记录已处理的消息体。
         *
         * @param body 消息体
         */
        @Override
        protected void handle(String body) {
            handledBodies.add(body);
        }
    }

}
