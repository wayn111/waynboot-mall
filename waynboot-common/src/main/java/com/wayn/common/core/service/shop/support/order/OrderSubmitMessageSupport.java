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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 下单消息投递支撑服务。
 * 统一封装异步下单消息和未支付延迟关单消息的 MQ 投递细节，编排层只表达何时发送哪类消息。
 * 本类只负责本地消息 Outbox 写入，不直接执行业务下单、关单或库存回补。
 */
@Slf4j
@Service
@AllArgsConstructor
public class OrderSubmitMessageSupport {

    private static final String ORDER_SUBMIT_KEY_PREFIX = "ORDER_SUBMIT:";
    private static final String ORDER_UNPAID_DELAY_KEY_PREFIX = "ORDER_UNPAID_DELAY:";
    private static final String ORDER_PAYLOAD_FIELD = "order";
    private static final String ORDER_SN_PAYLOAD_FIELD = "orderSn";
    private static final String NOTIFY_URL_PAYLOAD_FIELD = "notifyUrl";
    private static final String ORDER_SUBMIT_CALLBACK_PATH = "/callback/order/submit";
    private static final String ORDER_UNPAID_CALLBACK_PATH = "/callback/order/unpaid";

    private final LocalMessageService localMessageService;

    /**
     * 保存异步下单本地消息。
     * 入口线程不直接投递 RabbitMQ，先落本地消息表，后续由 relay 统一投递，避免 MQ 短暂不可用导致下单请求丢失。
     *
     * @param orderDTO 下单 DTO
     */
    public void sendSubmitMessage(OrderDTO orderDTO) {
        if (orderDTO == null || StringUtils.isBlank(orderDTO.getOrderSn())) {
            // messageKey 依赖订单号，缺失时写入 outbox 会形成无法幂等和无法补偿的毒消息。
            log.warn("跳过异步下单消息保存，订单上下文非法，orderDTO={}", orderDTO);
            return;
        }
        localMessageService.saveMessage(LocalMessageCreateCommand.builder()
                .messageKey(ORDER_SUBMIT_KEY_PREFIX + orderDTO.getOrderSn())
                .topic(LocalMessageTopics.ORDER_SUBMIT)
                .bizType(LocalMessageTopics.BIZ_TYPE_ORDER)
                .bizId(orderDTO.getOrderSn())
                .exchangeName(MQConstants.ORDER_DIRECT_EXCHANGE)
                .routingKey(MQConstants.ORDER_DIRECT_ROUTING)
                .payload(buildPayload(buildSubmitPayload(orderDTO)))
                .build());
    }

    /**
     * 保存未支付超时关单延迟本地消息。
     * 该方法在订单落库事务内调用，订单和延迟消息共同提交；事务回滚时不会产生孤儿关单消息。
     *
     * @param orderSn 订单号
     */
    public void saveUnpaidDelayMessage(String orderSn) {
        if (StringUtils.isBlank(orderSn)) {
            // 未支付关单消费端依赖订单号回查状态，空订单号不能进入延迟队列。
            log.warn("跳过未支付延迟关单消息保存，orderSn为空");
            return;
        }
        localMessageService.saveMessage(LocalMessageCreateCommand.builder()
                .messageKey(ORDER_UNPAID_DELAY_KEY_PREFIX + orderSn)
                .topic(LocalMessageTopics.ORDER_UNPAID_DELAY)
                .bizType(LocalMessageTopics.BIZ_TYPE_ORDER)
                .bizId(orderSn)
                .exchangeName(MQConstants.ORDER_DELAY_EXCHANGE)
                .routingKey(MQConstants.ORDER_DELAY_ROUTING)
                .payload(buildPayload(buildUnpaidDelayPayload(orderSn)))
                .delayMillis(calculateUnpaidDelayMillis())
                .build());
    }

    /**
     * 构建异步下单消息体。
     * 消费端依赖 order 字段还原完整下单请求，notifyUrl 用于兼容原有回调消费模型。
     *
     * @param orderDTO 下单 DTO
     * @return 下单消息体
     */
    private Map<String, Object> buildSubmitPayload(OrderDTO orderDTO) {
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put(ORDER_PAYLOAD_FIELD, orderDTO);
        messageBody.put(NOTIFY_URL_PAYLOAD_FIELD, buildMobileCallbackUrl(ORDER_SUBMIT_CALLBACK_PATH));
        return messageBody;
    }

    /**
     * 构建未支付延迟关单消息体。
     * 延迟消息只携带订单号，消费端需要重新查询订单状态，避免使用过期订单快照误关单。
     *
     * @param orderSn 订单号
     * @return 未支付延迟关单消息体
     */
    private Map<String, Object> buildUnpaidDelayPayload(String orderSn) {
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put(ORDER_SN_PAYLOAD_FIELD, orderSn);
        messageBody.put(NOTIFY_URL_PAYLOAD_FIELD, buildMobileCallbackUrl(ORDER_UNPAID_CALLBACK_PATH));
        return messageBody;
    }

    /**
     * 构建移动端回调地址。
     *
     * @param path 回调路径
     * @return 完整回调地址
     */
    private String buildMobileCallbackUrl(String path) {
        return WaynConfig.getMobileUrl() + path;
    }

    /**
     * 计算未支付订单延迟关单毫秒数。
     * 配置单位是分钟，MQ 延迟参数使用毫秒；这里集中转换，避免命令组装时混入单位换算细节。
     *
     * @return 延迟毫秒数
     */
    private int calculateUnpaidDelayMillis() {
        long delayTime = WaynConfig.getUnpaidOrderCancelDelayTime() * cn.hutool.core.date.DateUnit.MINUTE.getMillis();
        return Math.toIntExact(delayTime);
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
