package com.wayn.message.config;

import com.wayn.message.consumer.EmailSendConsumer;
import com.wayn.message.consumer.OrderPayConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工作模式rabbitmq配置
 */
@Configuration
public class WorkRabbitConfig {
    /*******************************************邮件消费者配置1个*****************************************/
    @Bean
    public EmailSendConsumer emailDirectReceiver() {
        return new EmailSendConsumer();
    }

    /*******************************************订单消费者配置2个*****************************************/
    @Bean
    public OrderPayConsumer orderWorkReceiver1() {
        return new OrderPayConsumer(1);
    }

    @Bean
    public OrderPayConsumer orderWorkReceiver2() {
        return new OrderPayConsumer(2);
    }

    @Bean
    public OrderPayConsumer orderWorkReceiver3() {
        return new OrderPayConsumer(3);
    }
}
