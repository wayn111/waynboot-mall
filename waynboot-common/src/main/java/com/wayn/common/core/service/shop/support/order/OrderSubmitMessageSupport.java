package com.wayn.common.core.service.shop.support.order;

import com.alibaba.fastjson.JSON;
import com.wayn.common.config.WaynConfig;
import com.wayn.common.core.service.message.LocalMessageCreateCommand;
import com.wayn.common.core.service.message.LocalMessageService;
import com.wayn.common.core.service.message.LocalMessageTopics;
import com.wayn.message.core.constant.MQConstants;
import com.wayn.message.core.dto.OrderDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private static final String ORDER_SUBMIT_KEY_PREFIX = "ORDER_SUBMIT:";
    private static final String ORDER_UNPAID_DELAY_KEY_PREFIX = "ORDER_UNPAID_DELAY:";

    private final LocalMessageService localMessageService;

    /**
     * 保存异步下单本地消息。
     * 入口线程不直接投递 RabbitMQ，先落本地消息表，后续由 relay 统一投递，避免 MQ 短暂不可用导致下单请求丢失。
     *
     * @param orderDTO 下单 DTO
     */
    public void sendSubmitMessage(OrderDTO orderDTO) {
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("order", orderDTO);
        messageBody.put("notifyUrl", WaynConfig.getMobileUrl() + "/callback/order/submit");
        localMessageService.saveMessage(LocalMessageCreateCommand.builder()
                .messageKey(ORDER_SUBMIT_KEY_PREFIX + orderDTO.getOrderSn())
                .topic(LocalMessageTopics.ORDER_SUBMIT)
                .bizType(LocalMessageTopics.BIZ_TYPE_ORDER)
                .bizId(orderDTO.getOrderSn())
                .exchangeName(MQConstants.ORDER_DIRECT_EXCHANGE)
                .routingKey(MQConstants.ORDER_DIRECT_ROUTING)
                .payload(buildPayload(messageBody))
                .build());
    }

    /**
     * 保存未支付超时关单延迟本地消息。
     * 该方法在订单落库事务内调用，订单和延迟消息共同提交；事务回滚时不会产生孤儿关单消息。
     *
     * @param orderSn 订单号
     */
    public void saveUnpaidDelayMessage(String orderSn) {
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("orderSn", orderSn);
        messageBody.put("notifyUrl", WaynConfig.getMobileUrl() + "/callback/order/unpaid");
        long delayTime = WaynConfig.getUnpaidOrderCancelDelayTime() * cn.hutool.core.date.DateUnit.MINUTE.getMillis();
        localMessageService.saveMessage(LocalMessageCreateCommand.builder()
                .messageKey(ORDER_UNPAID_DELAY_KEY_PREFIX + orderSn)
                .topic(LocalMessageTopics.ORDER_UNPAID_DELAY)
                .bizType(LocalMessageTopics.BIZ_TYPE_ORDER)
                .bizId(orderSn)
                .exchangeName(MQConstants.ORDER_DELAY_EXCHANGE)
                .routingKey(MQConstants.ORDER_DELAY_ROUTING)
                .payload(buildPayload(messageBody))
                .delayMillis(Math.toIntExact(delayTime))
                .build());
    }

    /**
     * 组装 JSON 消息体。
     *
     * @param messageBody 消息内容
     * @return JSON 消息体
     */
    private String buildPayload(Map<String, Object> messageBody) {
        return JSON.toJSONString(messageBody);
    }
}
