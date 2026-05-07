package com.wayn.message.consumer.mail;

import com.rabbitmq.client.Channel;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.message.consumer.client.mobile.MobileApi;
import com.wayn.message.consumer.support.AbstractSingleMessageConsumer;
import com.wayn.message.consumer.support.MessageConsumerSupport;
import com.wayn.message.core.constant.MQConstants;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.wayn.data.redis.constant.RedisKeyEnum.EMAIL_CONSUMER_MAP;

/**
 * 邮件发送消息消费入口。
 * 只声明邮件队列监听并调用 mobile 邮件回调，公共幂等、ack/nack 流程由单条消费模板统一处理。
 */
@Component
public class EmailSendConsumer extends AbstractSingleMessageConsumer {

    private final MobileApi mobileApi;

    /**
     * 构造邮件发送消费者。
     *
     * @param messageConsumerSupport MQ 消费支撑服务
     * @param mobileApi mobile 回调客户端
     */
    public EmailSendConsumer(MessageConsumerSupport messageConsumerSupport, MobileApi mobileApi) {
        super(messageConsumerSupport);
        this.mobileApi = mobileApi;
    }

    /**
     * 处理邮件发送消息。
     * 模板方法负责重复消息过滤、ack、nack 和幂等标记，本方法只保留 RabbitListener 入口契约。
     *
     * @param channel RabbitMQ 通道
     * @param message RabbitMQ 消息
     * @throws IOException ack/nack 通道异常
     */
    @RabbitListener(queues = MQConstants.EMAIL_DIRECT_QUEUE)
    public void process(Channel channel, Message message) throws IOException {
        // 邮件消息复用单条模板，确保幂等 Key 按 msgId 隔离，不再出现全局误判重复。
        consume(channel, message);
    }

    /**
     * 返回消费者名称。
     *
     * @return 消费者名称
     */
    @Override
    protected String consumerName() {
        return "EmailSendConsumer";
    }

    /**
     * 返回邮件消费幂等 Key。
     *
     * @return Redis 幂等 Key 枚举
     */
    @Override
    protected RedisKeyEnum redisKeyEnum() {
        return EMAIL_CONSUMER_MAP;
    }

    /**
     * 调用 mobile 邮件发送回调。
     *
     * @param body UTF-8 消息体
     * @throws Exception 回调失败
     */
    @Override
    protected void handle(String body) throws Exception {
        // 邮件发送仍由 mobile 回调承载，消费者不直接耦合邮件服务实现。
        mobileApi.sendEmail(body);
    }
}
