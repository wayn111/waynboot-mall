package com.wayn.message.config;

import com.wayn.message.reciver.EmailDirectReceiver;
import com.wayn.message.reciver.OrderDirectReceiver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工作模式rabbitmq配置
 */
@Configuration
public class WorkRabbitConfig {
    /*******************************************邮件消费者配置1个*****************************************/
    @Bean
    public EmailDirectReceiver emailDirectReceiver() {
        return new EmailDirectReceiver();
    }

    /*******************************************订单消费者配置2个*****************************************/
    @Bean
    public OrderDirectReceiver orderWorkReceiver1() {
        return new OrderDirectReceiver(1);
    }

    @Bean
    public OrderDirectReceiver orderWorkReceiver2() {
        return new OrderDirectReceiver(2);
    }

}
