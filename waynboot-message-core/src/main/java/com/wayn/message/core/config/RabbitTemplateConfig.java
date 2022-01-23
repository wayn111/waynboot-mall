package com.wayn.message.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * RabbitTemplate配置，设置生产者confirm确认和消费者手动确认
 */
@Slf4j
@Component
public class RabbitTemplateConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 设置开启Mandatory，才能触发回调函数,无论消息推送结果怎么样都强制调用回调函数
        rabbitTemplate.setMandatory(true);
        // 服务器收到消息确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            log.info("消息发送成功:correlationData({}),ack({}),cause({})", correlationData, ack, cause);
        });
        // 消息投递到队列失败回调处理
        rabbitTemplate.setReturnsCallback(returned -> {
            log.info("ReturnCallback:     " + "消息：" + returned.getMessage());
            log.info("ReturnCallback:     " + "回应码：" + returned.getReplyCode());
            log.info("ReturnCallback:     " + "回应信息：" + returned.getReplyText());
            log.info("ReturnCallback:     " + "交换机：" + returned.getExchange());
            log.info("ReturnCallback:     " + "路由键：" + returned.getRoutingKey());
        });

        return rabbitTemplate;
    }
}
