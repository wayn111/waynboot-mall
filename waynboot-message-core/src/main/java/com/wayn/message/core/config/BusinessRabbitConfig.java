package com.wayn.message.core.config;

import com.wayn.message.core.constant.MQConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 业务配置
 */
@Configuration
public class BusinessRabbitConfig {

    /************************************ 邮件队列、交换机 begin *******************************************/

    /**
     * 设置队列名称，并持久化
     *
     * @return 队列
     */
    @Bean
    public Queue emailDirectQueue() {
        return new Queue(MQConstants.EMAIL_DIRECT_QUEUE);
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

        Map<String, Object> params = new HashMap<>();
        // 声明当前队列绑定的死信交换机
        params.put("x-dead-letter-exchange", MQConstants.DL_TOPIC_EXCHANGE);
        // // 声明当前队列的死信路由键
        params.put("x-dead-letter-routing-key", MQConstants.DL_ROUTING_KEY);
        return new Queue(MQConstants.ORDER_DIRECT_QUEUE, true, false, false, params);
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
