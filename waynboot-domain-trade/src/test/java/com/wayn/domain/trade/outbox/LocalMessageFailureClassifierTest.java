package com.wayn.domain.trade.outbox;

import com.wayn.domain.api.outbox.enums.LocalMessageFailureReasonEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalMessageFailureClassifierTest {

    private final LocalMessageFailureClassifier classifier = new LocalMessageFailureClassifier();

    /**
     * RabbitMQ 或 AMQP 关键字应归类为消息队列不可用。
     */
    @Test
    void classifyRecognizesRabbitInfrastructureFailure() {
        assertEquals(LocalMessageFailureReasonEnum.RABBIT_UNAVAILABLE,
                classifier.classify("rabbit down").getReason());
        assertEquals(LocalMessageFailureReasonEnum.RABBIT_UNAVAILABLE,
                classifier.classify("AMQP connect failed").getReason());
    }

    /**
     * Redis 关键字应归类为缓存基础设施不可用。
     */
    @Test
    void classifyRecognizesRedisInfrastructureFailure() {
        assertEquals(LocalMessageFailureReasonEnum.REDIS_UNAVAILABLE,
                classifier.classify("redis connection timeout").getReason());
    }

    /**
     * 本地消息处理器缺失应归类为 handler 配置问题。
     */
    @Test
    void classifyRecognizesMissingLocalHandler() {
        assertEquals(LocalMessageFailureReasonEnum.HANDLER_NOT_FOUND,
                classifier.classify("未找到本地消息处理器, topic=ORDER").getReason());
    }

    /**
     * 无法识别或空异常信息时应落到 UNKNOWN，避免补偿日志丢失原始上下文。
     */
    @Test
    void classifyFallsBackToUnknown() {
        assertEquals(LocalMessageFailureReasonEnum.UNKNOWN,
                classifier.classify("unexpected").getReason());
        assertEquals(LocalMessageFailureReasonEnum.UNKNOWN,
                classifier.classify(null).getReason());
    }
}
