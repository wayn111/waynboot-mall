package com.wayn.common.core.service.shop.support.order;

import com.alibaba.fastjson.JSON;
import com.wayn.common.config.WaynConfig;
import com.wayn.message.core.constant.MQConstants;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.IdUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 下单消息投递支撑服务。
 * 统一封装异步下单消息和未支付延迟关单消息的 MQ 投递细节，编排层只表达何时发送哪类消息。
 */
@Slf4j
@Service
@AllArgsConstructor
public class OrderSubmitMessageSupport {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitTemplate delayRabbitTemplate;

    /**
     * 投递异步下单消息。
     *
     * @param orderDTO 下单 DTO
     */
    public void sendSubmitMessage(OrderDTO orderDTO) {
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("order", orderDTO);
        messageBody.put("notifyUrl", WaynConfig.getMobileUrl() + "/callback/order/submit");
        CorrelationData correlationData = new CorrelationData(IdUtil.getUid());
        try {
            rabbitTemplate.convertAndSend(MQConstants.ORDER_DIRECT_EXCHANGE, MQConstants.ORDER_DIRECT_ROUTING,
                    buildMessage(messageBody), correlationData);
        } catch (RuntimeException e) {
            log.error("发送异步下单消息失败", e);
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
        }
    }

    /**
     * 在当前事务提交后投递未支付超时关单延迟消息。
     * 如果当前没有事务同步上下文，则立即发送，便于单元测试和非事务调用路径复用。
     *
     * @param orderSn 订单号
     */
    public void sendUnpaidDelayMessageAfterCommit(String orderSn) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            sendUnpaidDelayMessageSafely(orderSn);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * 事务提交后发送未支付延迟关单消息。
             */
            @Override
            public void afterCommit() {
                sendUnpaidDelayMessageSafely(orderSn);
            }
        });
    }

    /**
     * 安全投递未支付延迟消息。
     * 该消息属于事务后副作用，发送失败只记录日志，避免订单主事务提交后因为 MQ 短暂异常导致消费端误判落单失败。
     *
     * @param orderSn 订单号
     */
    private void sendUnpaidDelayMessageSafely(String orderSn) {
        try {
            sendUnpaidDelayMessage(orderSn);
        } catch (RuntimeException e) {
            log.error("发送未支付关单延迟消息失败, orderSn={}", orderSn, e);
        }
    }

    /**
     * 投递未支付超时关单延迟消息。
     *
     * @param orderSn 订单号
     */
    private void sendUnpaidDelayMessage(String orderSn) {
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("orderSn", orderSn);
        messageBody.put("notifyUrl", WaynConfig.getMobileUrl() + "/callback/order/unpaid");
        delayRabbitTemplate.convertAndSend(MQConstants.ORDER_DELAY_EXCHANGE, MQConstants.ORDER_DELAY_ROUTING,
                buildMessage(messageBody), messagePostProcessor -> {
                    // 延迟关单消息只负责触发，实际取消仍在消费端通过状态条件更新防重。
                    long delayTime = WaynConfig.getUnpaidOrderCancelDelayTime() * cn.hutool.core.date.DateUnit.MINUTE.getMillis();
                    messagePostProcessor.getMessageProperties().setDelay(Math.toIntExact(delayTime));
                    return messagePostProcessor;
                });
    }

    /**
     * 组装 MQ 消息体。
     *
     * @param messageBody 消息内容
     * @return MQ 消息
     */
    private Message buildMessage(Map<String, Object> messageBody) {
        return MessageBuilder.withBody(JSON.toJSONString(messageBody).getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
    }
}
