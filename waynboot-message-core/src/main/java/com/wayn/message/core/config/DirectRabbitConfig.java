package com.wayn.message.core.config;

import com.wayn.message.core.constant.MQConstants;
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
    public Queue emailDirectQueue() {
        return new Queue(MQConstants.EMAIL_DIRECT_QUEUE, true);
    }

    /**
     * 设置交换机名称
     *
     * @return 交换机
     */
    @Bean
    DirectExchange emailDirectExchange() {
        return new DirectExchange(MQConstants.EMAIL_DIRECT_EXCHANGE);
    }

    /**
     * 将队列和交换机绑定, 并设置用于匹配键：EmailDirectRouting
     */
    @Bean
    Binding bindingEmailDirect() {
        return BindingBuilder.bind(emailDirectQueue()).to(emailDirectExchange()).with(MQConstants.EMAIL_DIRECT_ROUTING);
    }
    /************************************ 订单队列、交换机 end *******************************************/


    /************************************ 订单队列、交换机 begin *******************************************/
    @Bean
    public Queue orderDirectQueue() {
        return new Queue(MQConstants.ORDER_DIRECT_QUEUE, true);
    }

    @Bean
    DirectExchange orderDirectExchange() {
        return new DirectExchange(MQConstants.ORDER_DIRECT_EXCHANGE);
    }

    @Bean
    Binding bindingOrderDirect() {
        return BindingBuilder.bind(orderDirectQueue()).to(orderDirectExchange()).with(MQConstants.ORDER_DIRECT_ROUTING);
    }
    /************************************ 订单队列、交换机 end *******************************************/

}
