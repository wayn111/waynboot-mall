package com.wayn.message.consumer.order;

import com.wayn.message.consumer.support.MessageConsumerSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;

/**
 * 订单消息 ID 解析工具。
 * 统一兼容生产者回传 correlation header、AMQP messageId 和 deliveryTag，避免消息缺少 header 时幂等 key 退化为 null。
 *
 * @deprecated 使用 {@link MessageConsumerSupport#resolveMessageId(Message)} 统一所有消费者的幂等 ID 解析。
 */
@Deprecated
public final class OrderMessageIdSupport {

    private static final String RETURNED_MESSAGE_CORRELATION_HEADER = "spring_returned_message_correlation";

    private OrderMessageIdSupport() {
    }

    /**
     * 解析订单消费幂等 ID。
     *
     * @param message RabbitMQ 消息
     * @return 稳定的消息幂等 ID
     */
    public static String resolve(Message message) {
        String msgId = message.getMessageProperties().getHeader(RETURNED_MESSAGE_CORRELATION_HEADER);
        if (StringUtils.isNotBlank(msgId)) {
            return msgId;
        }
        msgId = message.getMessageProperties().getMessageId();
        if (StringUtils.isNotBlank(msgId)) {
            return msgId;
        }
        return String.valueOf(message.getMessageProperties().getDeliveryTag());
    }
}
