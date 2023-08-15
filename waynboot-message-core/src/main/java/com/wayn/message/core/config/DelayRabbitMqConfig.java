package com.wayn.message.core.config;

import com.wayn.message.core.constant.MQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息队列配置
 * Created by macro on 2018/9/14.
 */
@Configuration
public class DelayRabbitMqConfig {

    /**
     * 订单延迟插件消息队列所绑定的交换机
     */
    @Bean
    CustomExchange delayExchange() {
        // 创建一个自定义交换机，可以发送延迟消息
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(MQConstants.ORDER_DELAY_EXCHANGE, "x-delayed-message", true, false, args);
    }

    /**
     * 订单延迟插件队列
     */
    @Bean
    public Queue delayQueue() {
        return new Queue(MQConstants.ORDER_DELAY_QUEUE);
    }

    @Bean
    Binding delayBindingOrderDirect() {
        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with(MQConstants.ORDER_DELAY_ROUTING).noargs();
    }
}
