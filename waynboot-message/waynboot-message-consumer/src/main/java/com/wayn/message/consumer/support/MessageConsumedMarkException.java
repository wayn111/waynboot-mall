package com.wayn.message.consumer.support;

/**
 * 消息消费幂等标记异常。
 * 表示业务处理和 broker ack 已完成，但 Redis 幂等标记写入失败，模板需要记录告警而不能再 nack 已确认消息。
 */
public class MessageConsumedMarkException extends RuntimeException {

    /**
     * 构建消费幂等标记异常。
     *
     * @param message 异常信息
     * @param cause 原始异常
     */
    public MessageConsumedMarkException(String message, Throwable cause) {
        super(message, cause);
    }
}
