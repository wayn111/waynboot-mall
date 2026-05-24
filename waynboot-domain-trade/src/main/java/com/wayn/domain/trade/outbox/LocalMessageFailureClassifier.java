package com.wayn.domain.trade.outbox;

import com.wayn.domain.api.outbox.enums.LocalMessageFailureReasonEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 本地消息失败分类器。
 * 将异常文本归类为稳定原因编码，支撑补偿后台筛选、告警指标和死信分析。
 */
@Component
public class LocalMessageFailureClassifier {

    /**
     * 识别失败原因。
     *
     * @param errorMessage 原始异常信息
     * @return 失败分类结果
     */
    public LocalMessageFailureClassification classify(String errorMessage) {
        String safeMessage = StringUtils.defaultString(errorMessage);
        String lowerMessage = safeMessage.toLowerCase(Locale.ROOT);
        if (isRabbitFailure(lowerMessage)) {
            return buildClassification(LocalMessageFailureReasonEnum.RABBIT_UNAVAILABLE, safeMessage);
        }
        if (isRedisFailure(lowerMessage)) {
            return buildClassification(LocalMessageFailureReasonEnum.REDIS_UNAVAILABLE, safeMessage);
        }
        if (isHandlerNotFound(safeMessage)) {
            return buildClassification(LocalMessageFailureReasonEnum.HANDLER_NOT_FOUND, safeMessage);
        }
        return buildClassification(LocalMessageFailureReasonEnum.UNKNOWN, safeMessage);
    }

    /**
     * 判断是否为 RabbitMQ/AMQP 基础设施异常。
     *
     * @param lowerMessage 小写异常文本
     * @return true=RabbitMQ/AMQP 异常
     */
    private boolean isRabbitFailure(String lowerMessage) {
        return lowerMessage.contains("rabbit") || lowerMessage.contains("amqp");
    }

    /**
     * 判断是否为 Redis 基础设施异常。
     *
     * @param lowerMessage 小写异常文本
     * @return true=Redis 异常
     */
    private boolean isRedisFailure(String lowerMessage) {
        return lowerMessage.contains("redis");
    }

    /**
     * 判断是否为本地 handler 缺失异常。
     *
     * @param message 原始异常文本
     * @return true=本地 handler 缺失
     */
    private boolean isHandlerNotFound(String message) {
        return message.contains("未找到本地消息处理器");
    }

    /**
     * 构建失败分类结果。
     *
     * @param reason 失败原因
     * @param summary 错误摘要
     * @return 失败分类结果
     */
    private LocalMessageFailureClassification buildClassification(LocalMessageFailureReasonEnum reason,
                                                                  String summary) {
        return new LocalMessageFailureClassification(reason, summary);
    }
}
