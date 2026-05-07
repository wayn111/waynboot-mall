package com.wayn.message.consumer.mail;

import com.rabbitmq.client.Channel;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.message.consumer.client.mobile.MobileApi;
import com.wayn.message.consumer.support.MessageConsumerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;

import static com.wayn.data.redis.constant.RedisKeyEnum.EMAIL_CONSUMER_MAP;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailSendConsumerTest {

    @Test
    void processUsesMessageIdScopedIdempotencyKey() throws Exception {
        RedisCache redisCache = mock(RedisCache.class);
        MobileApi mobileApi = mock(MobileApi.class);
        Channel channel = mock(Channel.class);
        MessageConsumerSupport messageConsumerSupport = new MessageConsumerSupport(redisCache);
        EmailSendConsumer consumer = new EmailSendConsumer(messageConsumerSupport, mobileApi);
        Message duplicateMessage = buildMessage(1L, "mail-1", "duplicate");
        Message pendingMessage = buildMessage(2L, "mail-2", "pending");
        when(redisCache.getCacheObject(EMAIL_CONSUMER_MAP.getKey("mail-1"))).thenReturn("mail-1");
        when(redisCache.getCacheObject(EMAIL_CONSUMER_MAP.getKey("mail-2"))).thenReturn(null);

        consumer.process(channel, duplicateMessage);
        consumer.process(channel, pendingMessage);

        verify(channel).basicAck(1L, false);
        verify(mobileApi, never()).sendEmail("duplicate");
        verify(mobileApi).sendEmail("pending");
        verify(channel).basicAck(2L, false);
        verify(redisCache).setCacheObject(EMAIL_CONSUMER_MAP.getKey("mail-2"),
                "mail-2", EMAIL_CONSUMER_MAP.getExpireSecond());
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
