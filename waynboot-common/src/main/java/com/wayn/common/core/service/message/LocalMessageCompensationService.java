package com.wayn.common.core.service.message;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.message.LocalMessage;
import com.wayn.common.core.enums.LocalMessageStatusEnum;
import com.wayn.common.core.mapper.message.LocalMessageMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 本地消息补偿运营服务。
 * 提供失败消息查询和人工重投能力，避免直接改表破坏本地消息状态机和重试元数据。
 */
@Service
@AllArgsConstructor
public class LocalMessageCompensationService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 500;
    private static final String SYSTEM_OPERATOR = "system";
    private static final String MANUAL_RETRY_ERROR_PREFIX = "人工重投, operator=";

    private final LocalMessageMapper localMessageMapper;
    private final LocalMessageCompensationLogService compensationLogService;

    /**
     * 查询失败本地消息。
     *
     * @param limit 查询数量
     * @return 失败消息列表
     */
    public List<LocalMessage> listFailedMessages(int limit) {
        int safeLimit = normalizeLimit(limit);
        List<LocalMessage> messages = localMessageMapper.selectList(Wrappers.lambdaQuery(LocalMessage.class)
                .eq(LocalMessage::getStatus, LocalMessageStatusEnum.FAILED.getStatus())
                .orderByDesc(LocalMessage::getUpdateTime)
                .last("limit " + safeLimit));
        return CollectionUtils.emptyIfNull(messages).stream().toList();
    }

    /**
     * 人工重投失败消息。
     * 只允许 FAILED -> INIT，避免把正在自动重试或已成功消息重新投递导致重复副作用。
     *
     * @param messageId 本地消息 ID
     * @param operator 操作者
     * @return true=重投状态重置成功
     */
    public boolean retryFailedMessage(Long messageId, String operator) {
        if (messageId == null) {
            // 空 ID 属于无效运营请求，必须在入口截断，避免生成 id is null 的更新条件。
            return false;
        }
        Date now = new Date();
        String safeOperator = normalizeOperator(operator);
        LocalMessage originalMessage = findOriginalMessage(messageId);
        boolean reset = resetFailedMessageToInit(messageId, safeOperator, now);
        recordManualRetryLogIfNecessary(originalMessage, safeOperator, reset);
        return reset;
    }

    /**
     * 规范化人工操作人。
     * 操作人会写入人工重投日志和 lastError 字段，统一裁剪空白可避免同一账号在审计统计中分裂成多个值。
     *
     * @param operator 原始操作者
     * @return 非空操作者
     */
    private String normalizeOperator(String operator) {
        return StringUtils.isBlank(operator) ? SYSTEM_OPERATOR : operator.trim();
    }

    /**
     * 查询重投前的原始消息。
     * 原始消息只用于记录人工重投日志，真正的状态安全性仍由后续条件更新保证。
     *
     * @param messageId 本地消息 ID
     * @return 原始消息，不存在时返回 null
     */
    private LocalMessage findOriginalMessage(Long messageId) {
        return localMessageMapper.selectById(messageId);
    }

    /**
     * 将失败消息重置为待投递状态。
     * 条件更新只允许 FAILED -> INIT，避免重复重投仍在自动重试中的消息。
     *
     * @param messageId 本地消息 ID
     * @param operator 操作者
     * @param now 当前时间
     * @return true=状态重置成功
     */
    private boolean resetFailedMessageToInit(Long messageId, String operator, Date now) {
        int updated = localMessageMapper.update(null, Wrappers.lambdaUpdate(LocalMessage.class)
                .set(LocalMessage::getStatus, LocalMessageStatusEnum.INIT.getStatus())
                .set(LocalMessage::getRetryCount, 0)
                .set(LocalMessage::getNextRetryTime, now)
                .set(LocalMessage::getLastError, MANUAL_RETRY_ERROR_PREFIX + operator)
                .set(LocalMessage::getUpdateTime, now)
                .eq(LocalMessage::getId, messageId)
                .eq(LocalMessage::getStatus, LocalMessageStatusEnum.FAILED.getStatus()));
        return updated > 0;
    }

    /**
     * 按需记录人工重投日志。
     * 只有状态重置成功且原消息存在时才记录日志，避免更新失败时产生误导性的运营记录。
     *
     * @param originalMessage 重投前消息
     * @param operator 操作者
     * @param reset 是否重置成功
     */
    private void recordManualRetryLogIfNecessary(LocalMessage originalMessage, String operator, boolean reset) {
        if (!reset || originalMessage == null) {
            return;
        }
        compensationLogService.recordManualRetry(originalMessage, operator);
    }

    /**
     * 统计当前死信失败消息数量。
     * 该指标可被管理端接口或监控采集器使用，用于判断本地消息补偿是否积压。
     *
     * @return 补偿指标
     */
    public LocalMessageCompensationMetric countFailedMessages() {
        Long failedCount = localMessageMapper.selectCount(Wrappers.lambdaQuery(LocalMessage.class)
                .eq(LocalMessage::getStatus, LocalMessageStatusEnum.FAILED.getStatus()));
        LocalMessageCompensationMetric metric = new LocalMessageCompensationMetric();
        metric.setFailedCount(failedCount == null ? 0L : failedCount);
        return metric;
    }

    /**
     * 规范化查询数量。
     *
     * @param limit 原始查询数量
     * @return 安全查询数量
     */
    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
