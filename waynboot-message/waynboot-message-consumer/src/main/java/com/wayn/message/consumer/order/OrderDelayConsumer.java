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

import static com.wayn.data.redis.constant.RedisKeyEnum.UNPAID_ORDER_CONSUMER_MAP;

/**
 * 未支付订单延迟消息消费入口。
 * 只声明延迟关单队列监听并调用 mobile 回调，公共幂等、ack/nack 流程由单条消费模板统一处理。
 */
@Component
public class OrderDelayConsumer extends AbstractSingleMessageConsumer {
    private final MobileApi mobileApi;

    /**
     * 构造未支付订单延迟消费者。
     *
     * @param mobileApi mobile 回调客户端
     * @param messageConsumerSupport MQ 消费支撑服务
     */
    public OrderDelayConsumer(MobileApi mobileApi, MessageConsumerSupport messageConsumerSupport) {
        super(messageConsumerSupport);
        this.mobileApi = mobileApi;
    }

    /**
     * 处理未支付超时关单消息。
     * 模板方法负责重复消息过滤、ack、nack 和幂等标记，本方法只保留 RabbitListener 入口契约。
     *
     * @param channel RabbitMQ 通道
     * @param message RabbitMQ 消息
     * @throws IOException ack/nack 通道异常
     */
    @RabbitListener(queues = MQConstants.ORDER_DELAY_QUEUE)
    public void process(Channel channel, Message message) throws IOException {
        // 延迟关单仍保持单条消费，避免把不同订单的关单失败互相影响。
        consume(channel, message);
    }

    /**
     * 返回消费者名称。
     *
     * @return 消费者名称
     */
    @Override
    protected String consumerName() {
        return "OrderDelayConsumer";
    }

    /**
     * 返回未支付订单消费幂等 Key。
     *
     * @return Redis 幂等 Key 枚举
     */
    @Override
    protected RedisKeyEnum redisKeyEnum() {
        return UNPAID_ORDER_CONSUMER_MAP;
    }

    /**
     * 调用 mobile 未支付关单回调。
     *
     * @param body UTF-8 消息体
     * @throws Exception 回调失败
     */
    @Override
    protected void handle(String body) throws Exception {
        // 实际取消订单逻辑在 mobile/common 侧处理，消费者只负责触发回调。
        mobileApi.unpaidOrder(body);
    }
}
