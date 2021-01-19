package com.wayn.message.core.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 直连交换机配置
 */
@Configuration
public class DirectRabbitConfig {

    // 队列 起名：EmailDirectQueue
    @Bean
    public Queue EmailDirectQueue() {
        return new Queue("EmailDirectQueue", true);
    }

    // Direct交换机 起名：EmailDirectExchange
    @Bean
    DirectExchange EmailDirectExchange() {
        return new DirectExchange("EmailDirectExchange");
    }

    // 绑定  将队列和交换机绑定, 并设置用于匹配键：EmailDirectRouting
    @Bean
    Binding bindingTestDirect() {
        return BindingBuilder.bind(EmailDirectQueue()).to(EmailDirectExchange()).with("EmailDirectRouting");
    }

    @Bean
    public Queue OrderDirectQueue() {
        return new Queue("OrderDirectQueue", true);
    }

    @Bean
    DirectExchange OrderDirectExchange() {
        return new DirectExchange("OrderDirectExchange");
    }

    @Bean
    Binding bindingOrderDirect() {
        return BindingBuilder.bind(OrderDirectQueue()).to(OrderDirectExchange()).with("OrderDirectRouting");
    }

}
