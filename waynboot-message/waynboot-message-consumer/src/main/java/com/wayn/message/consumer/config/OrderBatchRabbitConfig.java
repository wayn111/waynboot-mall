package com.wayn.message.consumer.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单批量消费 RabbitMQ 配置。
 * 仅对显式指定该 factory 的订单消费者生效，避免修改 application.yml 影响邮件、延迟关单等其他消费者。
 */
@Configuration
public class OrderBatchRabbitConfig {

    private static final int ORDER_BATCH_SIZE = 20;
    private static final int ORDER_PREFETCH_COUNT = 100;
    private static final int ORDER_CONCURRENT_CONSUMERS = 2;
    private static final int ORDER_MAX_CONCURRENT_CONSUMERS = 8;
    private static final long ORDER_RECEIVE_TIMEOUT_MILLIS = 200L;

    /**
     * 构建订单批量消费监听容器。
     * 使用手动 ack 保留现有幂等语义，通过批量拉取和并发消费者提升高峰下单吞吐。
     *
     * @param configurer Spring Boot Rabbit 监听容器配置器
     * @param connectionFactory RabbitMQ 连接工厂
     * @return 订单批量监听容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory orderBatchRabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setBatchListener(true);
        factory.setConsumerBatchEnabled(true);
        factory.setBatchSize(ORDER_BATCH_SIZE);
        factory.setPrefetchCount(ORDER_PREFETCH_COUNT);
        factory.setConcurrentConsumers(ORDER_CONCURRENT_CONSUMERS);
        factory.setMaxConcurrentConsumers(ORDER_MAX_CONCURRENT_CONSUMERS);
        factory.setReceiveTimeout(ORDER_RECEIVE_TIMEOUT_MILLIS);
        return factory;
    }
}
