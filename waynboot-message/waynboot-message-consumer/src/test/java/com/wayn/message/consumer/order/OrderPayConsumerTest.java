package com.wayn.message.consumer.order;

import com.rabbitmq.client.Channel;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.message.consumer.client.mobile.MobileApi;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.wayn.data.redis.constant.RedisKeyEnum.ORDER_CONSUMER_MAP;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderPayConsumerTest {

    @Test
    void processBatchAcksDuplicatesAndSubmitsOnlyPendingMessages() throws Exception {
        RedisCache redisCache = mock(RedisCache.class);
        MobileApi mobileApi = mock(MobileApi.class);
        Channel channel = mock(Channel.class);
        OrderPayConsumer consumer = new OrderPayConsumer();
        ReflectionTestUtils.setField(consumer, "redisCache", redisCache);
        ReflectionTestUtils.setField(consumer, "mobileApi", mobileApi);
        Message duplicateMessage = buildMessage(1L, "msg-1", "duplicate");
        Message pendingMessage = buildMessage(2L, "msg-2", "pending");
        when(redisCache.getCacheObject(ORDER_CONSUMER_MAP.getKey("msg-1"))).thenReturn("msg-1");
        when(redisCache.getCacheObject(ORDER_CONSUMER_MAP.getKey("msg-2"))).thenReturn(null);

        consumer.process(List.of(duplicateMessage, pendingMessage), channel);

        verify(channel).basicAck(1L, false);
        verify(mobileApi).submitOrders(List.of("pending"));
        verify(channel).basicAck(2L, false);
        verify(redisCache).setCacheObject(ORDER_CONSUMER_MAP.getKey("msg-2"),
                "msg-2", ORDER_CONSUMER_MAP.getExpireSecond());
        verify(mobileApi, never()).submitOrder("pending");
    }

    @Test
    void processBatchNacksPendingMessagesWhenBatchSubmitFailed() throws Exception {
        RedisCache redisCache = mock(RedisCache.class);
        MobileApi mobileApi = mock(MobileApi.class);
        Channel channel = mock(Channel.class);
        OrderPayConsumer consumer = new OrderPayConsumer();
        ReflectionTestUtils.setField(consumer, "redisCache", redisCache);
        ReflectionTestUtils.setField(consumer, "mobileApi", mobileApi);
        Message pendingMessage = buildMessage(3L, "msg-3", "pending");
        when(redisCache.getCacheObject(ORDER_CONSUMER_MAP.getKey("msg-3"))).thenReturn(null);
        org.mockito.Mockito.doThrow(new RuntimeException("callback error"))
                .when(mobileApi).submitOrders(List.of("pending"));

        consumer.process(List.of(pendingMessage), channel);

        verify(channel).basicNack(3L, false, false);
        verify(channel, never()).basicAck(3L, false);
        verify(redisCache, never()).setCacheObject(eq(ORDER_CONSUMER_MAP.getKey("msg-3")),
                eq("msg-3"), eq(ORDER_CONSUMER_MAP.getExpireSecond()));
    }

    private Message buildMessage(long deliveryTag, String msgId, String body) {
        return MessageBuilder.withBody(body.getBytes(StandardCharsets.UTF_8))
                .setHeader("spring_returned_message_correlation", msgId)
                .setDeliveryTag(deliveryTag)
                .setMessageId(msgId)
                .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                .build();
    }
}
