package com.wayn.message.consumer.order;


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

import static com.wayn.data.redis.constant.RedisKeyEnum.ORDER_CONSUMER_MAP;

/**
 * 支付订单消费入口。
 * 负责声明订单队列监听和 mobile 单笔下单回调，公共幂等、ack/nack 流程由单条消费模板统一处理。
 */
@Component
public class OrderPayConsumer extends AbstractSingleMessageConsumer {

    private final MobileApi mobileApi;

    /**
     * 构造支付订单消费者。
     *
     * @param messageConsumerSupport MQ 消费支撑服务
     * @param mobileApi mobile 回调客户端
     */
    public OrderPayConsumer(MessageConsumerSupport messageConsumerSupport, MobileApi mobileApi) {
        super(messageConsumerSupport);
        this.mobileApi = mobileApi;
    }

    /**
     * 处理支付订单消息。
     * 模板方法负责重复消息过滤、ack、nack 和幂等标记，本方法只保留 RabbitListener 入口契约。
     *
     * @param channel RabbitMQ 通道
     * @param message RabbitMQ 消息
     * @throws IOException ack/nack 通道异常
     */
    @RabbitListener(queues = MQConstants.ORDER_DIRECT_QUEUE)
    public void process(Channel channel, Message message) throws IOException {
        // 单笔消费让每个订单独立 ack/nack，避免批量回调失败扩大影响面。
        consume(channel, message);
    }

    /**
     * 返回消费者名称。
     *
     * @return 消费者名称
     */
    @Override
    protected String consumerName() {
        return "OrderPayConsumer";
    }

    /**
     * 返回订单消费幂等 Key。
     *
     * @return Redis 幂等 Key 枚举
     */
    @Override
    protected RedisKeyEnum redisKeyEnum() {
        return ORDER_CONSUMER_MAP;
    }

    /**
     * 调用 mobile 单笔下单回调。
     *
     * @param body UTF-8 消息体
     * @throws Exception 回调失败
     */
    @Override
    protected void handle(String body) throws Exception {
        // 订单服务已收口为单笔同步落单，消费者只转发当前消息体。
        mobileApi.submitOrder(body);
    }
}
