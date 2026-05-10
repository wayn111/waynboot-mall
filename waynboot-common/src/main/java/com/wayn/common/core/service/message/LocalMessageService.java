package com.wayn.common.core.service.message;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.message.LocalMessage;
import com.wayn.common.core.enums.LocalMessageStatusEnum;
import com.wayn.common.core.mapper.message.LocalMessageMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 本地消息服务。
 * 负责本地消息创建、待投递查询和状态流转，业务侧不直接操作本地消息表状态字段。
 */
@Slf4j
@Service
@AllArgsConstructor
public class LocalMessageService {

    private static final int DEFAULT_DELAY_MILLIS = 0;
    private static final int MAX_RETRY_COUNT = 5;
    private static final int MAX_ERROR_BYTES = 1000;
    private static final long BASE_RETRY_DELAY_MILLIS = 1000L;
    private static final long MAX_RETRY_DELAY_MILLIS = 60_000L;

    private final LocalMessageMapper localMessageMapper;

    /**
     * 保存本地消息。
     * 该方法应在业务事务内调用，业务数据和本地消息一起提交；重复 messageKey 视为幂等成功。
     *
     * @param command 本地消息创建命令
     */
    public void saveMessage(LocalMessageCreateCommand command) {
        LocalMessage message = buildInitialMessage(command);
        try {
            localMessageMapper.insert(message);
        } catch (DuplicateKeyException e) {
            // messageKey 建议建唯一索引。重复写入说明业务已产生同一副作用消息，按幂等成功处理。
            log.info("本地消息已存在, messageKey={}", command.messageKey());
        }
    }

    /**
     * 查询到期待投递消息。
     *
     * @param limit 查询数量
     * @return 到期待投递消息列表
     */
    public List<LocalMessage> listPendingMessages(int limit) {
        int safeLimit = Math.max(1, limit);
        return localMessageMapper.selectList(Wrappers.lambdaQuery(LocalMessage.class)
                .eq(LocalMessage::getStatus, LocalMessageStatusEnum.INIT.getStatus())
                .le(LocalMessage::getNextRetryTime, new Date())
                .orderByAsc(LocalMessage::getNextRetryTime)
                .last("limit " + safeLimit));
    }

    /**
     * 标记消息已成功投递或处理。
     *
     * @param messageId 本地消息 ID
     */
    public void markSent(Long messageId) {
        Date now = new Date();
        LocalMessage update = new LocalMessage();
        update.setStatus(LocalMessageStatusEnum.SENT.getStatus());
        update.setSentTime(now);
        update.setUpdateTime(now);
        localMessageMapper.update(null, Wrappers.lambdaUpdate(LocalMessage.class)
                .set(LocalMessage::getStatus, update.getStatus())
                .set(LocalMessage::getSentTime, update.getSentTime())
                .set(LocalMessage::getUpdateTime, update.getUpdateTime())
                .eq(LocalMessage::getId, messageId)
                .eq(LocalMessage::getStatus, LocalMessageStatusEnum.INIT.getStatus()));
    }

    /**
     * 标记消息投递失败并计算下一次重试时间。
     *
     * @param message 本地消息
     * @param errorMessage 失败原因
     */
    public void markFailed(LocalMessage message, String errorMessage) {
        int nextRetryCount = defaultRetryCount(message.getRetryCount()) + 1;
        Short nextStatus = nextRetryCount >= MAX_RETRY_COUNT
                ? LocalMessageStatusEnum.FAILED.getStatus()
                : LocalMessageStatusEnum.INIT.getStatus();
        Date now = new Date();
        Date nextRetryTime = new Date(now.getTime() + calculateRetryDelayMillis(nextRetryCount));
        localMessageMapper.update(null, Wrappers.lambdaUpdate(LocalMessage.class)
                .set(LocalMessage::getStatus, nextStatus)
                .set(LocalMessage::getRetryCount, nextRetryCount)
                .set(LocalMessage::getNextRetryTime, nextRetryTime)
                .set(LocalMessage::getLastError, truncateError(errorMessage))
                .set(LocalMessage::getUpdateTime, now)
                .eq(LocalMessage::getId, message.getId())
                .eq(LocalMessage::getStatus, LocalMessageStatusEnum.INIT.getStatus()));
    }

    /**
     * 组装初始本地消息实体。
     *
     * @param command 本地消息创建命令
     * @return 初始本地消息实体
     */
    private LocalMessage buildInitialMessage(LocalMessageCreateCommand command) {
        Date now = new Date();
        LocalMessage message = new LocalMessage();
        message.setMessageKey(command.messageKey());
        message.setTopic(command.topic());
        message.setBizType(command.bizType());
        message.setBizId(command.bizId());
        message.setExchangeName(command.exchangeName());
        message.setRoutingKey(command.routingKey());
        message.setPayload(command.payload());
        message.setDelayMillis(command.delayMillis() == null ? DEFAULT_DELAY_MILLIS : command.delayMillis());
        message.setStatus(LocalMessageStatusEnum.INIT.getStatus());
        message.setRetryCount(0);
        message.setNextRetryTime(now);
        message.setCreateTime(now);
        message.setUpdateTime(now);
        return message;
    }

    /**
     * 计算指数退避重试间隔。
     *
     * @param retryCount 当前重试次数
     * @return 重试间隔毫秒数
     */
    private long calculateRetryDelayMillis(int retryCount) {
        long delay = BASE_RETRY_DELAY_MILLIS * (1L << Math.min(retryCount, 6));
        return Math.min(delay, MAX_RETRY_DELAY_MILLIS);
    }

    /**
     * 返回安全重试次数。
     *
     * @param retryCount 当前重试次数
     * @return 非空重试次数
     */
    private int defaultRetryCount(Integer retryCount) {
        return retryCount == null ? 0 : retryCount;
    }

    /**
     * 截断错误信息。
     *
     * @param errorMessage 错误信息
     * @return 截断后的错误信息
     */
    private String truncateError(String errorMessage) {
        if (StringUtils.isBlank(errorMessage)) {
            return "";
        }
        byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= MAX_ERROR_BYTES) {
            return errorMessage;
        }
        return errorMessage.substring(0, Math.min(errorMessage.length(), MAX_ERROR_BYTES / 3));
    }
}
