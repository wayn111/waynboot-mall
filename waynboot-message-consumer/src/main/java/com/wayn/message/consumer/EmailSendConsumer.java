package com.wayn.message.consumer;

import com.rabbitmq.client.Channel;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.message.api.MobileApi;
import com.wayn.message.core.constant.MQConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.wayn.data.redis.constant.RedisKeyEnum.EMAIL_CONSUMER_MAP;

@Slf4j
@Component
public class EmailSendConsumer {

    @Resource
    private MobileApi mobileApi;
    @Resource
    private RedisCache redisCache;

    @RabbitListener(queues = MQConstants.EMAIL_DIRECT_QUEUE)
    public void process(Channel channel, Message message) throws IOException {
        log.info("EmailDirectReceiver 消费者收到消息: {}", message.toString());
        String body = new String(message.getBody());
        // spring_listener_return_correlation
        String msgId = message.getMessageProperties().getHeader("spring_returned_message_correlation");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // 消费者消费消息时幂等性处理
        if (redisCache.getCacheObject(EMAIL_CONSUMER_MAP.getKey()) != null) {
            // redis中包含该 key，说明该消息已经被消费过
            log.error("msgId: {}，消息已经被消费", msgId);
            channel.basicAck(deliveryTag, false);// 确认消息已消费
            return;
        }

        try {
            mobileApi.sendEmail(body);
            // multiple参数：确认收到消息，false只确认当前consumer一个消息收到，true确认所有consumer获得的消息
            channel.basicAck(deliveryTag, false);
            redisCache.setCacheObject(EMAIL_CONSUMER_MAP.getKey(), msgId, EMAIL_CONSUMER_MAP.getExpireSecond());
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, false);
            log.error(e.getMessage(), e);
        }
    }
}
