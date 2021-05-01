package com.wayn.message.core.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * RabbitTemplate配置，设置生产者confirm确认和消费者手动确认
 */
@Component
public class RabbitTemplateConfig {

    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        // 设置开启Mandatory，才能触发回调函数,无论消息推送结果怎么样都强制调用回调函数
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            System.out.println("ConfirmCallback:     " + "相关数据：" + correlationData);
            System.out.println("ConfirmCallback:     " + "确认情况：" + ack);
            System.out.println("ConfirmCallback:     " + "原因：" + cause);
        });
        rabbitTemplate.setReturnsCallback(returned -> {
            System.out.println("ReturnCallback:     " + "消息：" + returned.getMessage());
            System.out.println("ReturnCallback:     " + "回应码：" + returned.getReplyCode());
            System.out.println("ReturnCallback:     " + "回应信息：" + returned.getReplyText());
            System.out.println("ReturnCallback:     " + "交换机：" + returned.getExchange());
            System.out.println("ReturnCallback:     " + "路由键：" + returned.getRoutingKey());
        });
        return rabbitTemplate;
    }
}
