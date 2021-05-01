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

    /************************************ 订单队列、交换机 begin *******************************************/

    /**
     * 设置队列名称，并持久化
     *
     * @return 队列
     */
    @Bean
    public Queue EmailDirectQueue() {
        return new Queue("EmailDirectQueue", true);
    }

    /**
     * 设置交换机名称
     *
     * @return 交换机
     */
    @Bean
    DirectExchange EmailDirectExchange() {
        return new DirectExchange("EmailDirectExchange");
    }

    /**
     * 将队列和交换机绑定, 并设置用于匹配键：EmailDirectRouting
     */
    @Bean
    Binding bindingEmailDirect() {
        return BindingBuilder.bind(EmailDirectQueue()).to(EmailDirectExchange()).with("EmailDirectRouting");
    }
    /************************************ 订单队列、交换机 end *******************************************/


    /************************************ 订单队列、交换机 begin *******************************************/
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
    /************************************ 订单队列、交换机 end *******************************************/

}
