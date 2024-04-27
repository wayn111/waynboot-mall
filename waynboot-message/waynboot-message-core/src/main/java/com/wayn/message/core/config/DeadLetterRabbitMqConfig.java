package com.wayn.message.core.config;

import com.wayn.message.core.constant.MQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 消息队列配置
 * Created by macro on 2018/9/14.
 */
@Configuration
public class DeadLetterRabbitMqConfig {

    // 创建交换机
    @Bean
    public TopicExchange dlTopicExchange() {
        return new TopicExchange(MQConstants.DL_TOPIC_EXCHANGE);
    }

    // 创建队列
    @Bean
    public Queue dlQueue() {
        return new Queue(MQConstants.DL_QUEUE);
    }

    // 队列与交换机进行绑定
    @Bean
    public Binding BindingDlQueueAndExchange(Queue dlQueue, TopicExchange dlTopicExchange) {
        return BindingBuilder.bind(dlQueue).to(dlTopicExchange).with(MQConstants.DL_ROUTING_KEY);
    }

}
