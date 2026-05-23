package com.wayn.common.core.service.message;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.message.LocalMessage;
import com.wayn.common.core.enums.LocalMessageStatusEnum;
import com.wayn.common.core.mapper.message.LocalMessageMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

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
    private final LocalMessageFailureClassifier failureClassifier;
    private final LocalMessageCompensationLogService compensationLogService;

    /**
     * 保存本地消息。
     * 该方法应在业务事务内调用，业务数据和本地消息一起提交；重复 messageKey 视为幂等成功。
     *
     * @param command 本地消息创建命令
     */
    public void saveMessage(LocalMessageCreateCommand command) {
        if (!isValidCreateCommand(command)) {
            log.warn("跳过本地消息保存，创建命令非法，command={}", command);
            return;
        }
        LocalMessage message = buildInitialMessage(command);
        try {
            localMessageMapper.insert(message);
        } catch (DuplicateKeyException e) {
            // messageKey 建议建唯一索引。重复写入说明业务已产生同一副作用消息，按幂等成功处理。
            log.info("本地消息已存在, messageKey={}", command.messageKey());
        }
    }

    /**
     * 校验本地消息创建命令。
     * messageKey 是 outbox 幂等、relay 去重和补偿定位的核心字段，缺失时不能写入本地消息表。
     *
     * @param command 本地消息创建命令
     * @return true=命令可用于创建本地消息
     */
    private boolean isValidCreateCommand(LocalMessageCreateCommand command) {
        return command != null && StringUtils.isNotBlank(command.messageKey());
    }

    /**
     * 查询到期待投递消息。
     * 返回值保持空列表语义，relay 批处理入口可以直接遍历，不需要重复做 null 防御。
     *
     * @param limit 查询数量
     * @return 到期待投递消息列表
     */
    public List<LocalMessage> listPendingMessages(int limit) {
        int safeLimit = Math.max(1, limit);
        List<LocalMessage> messages = localMessageMapper.selectList(Wrappers.lambdaQuery(LocalMessage.class)
                .eq(LocalMessage::getStatus, LocalMessageStatusEnum.INIT.getStatus())
                .le(LocalMessage::getNextRetryTime, new Date())
                .orderByAsc(LocalMessage::getNextRetryTime)
                .last("limit " + safeLimit));
        return CollectionUtils.emptyIfNull(messages).stream().toList();
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
     * 状态仍为 INIT 时会继续等待 relay 扫描；达到最大重试次数后进入 FAILED，由补偿任务或人工治理接管。
     *
     * @param message 本地消息
     * @param errorMessage 失败原因
     */
    public void markFailed(LocalMessage message, String errorMessage) {
        Date now = new Date();
        RetryDecision retryDecision = buildRetryDecision(message, now);
        localMessageMapper.update(null, Wrappers.lambdaUpdate(LocalMessage.class)
                .set(LocalMessage::getStatus, retryDecision.nextStatus())
                .set(LocalMessage::getRetryCount, retryDecision.nextRetryCount())
                .set(LocalMessage::getNextRetryTime, retryDecision.nextRetryTime())
                .set(LocalMessage::getLastError, truncateError(errorMessage))
                .set(LocalMessage::getUpdateTime, now)
                .eq(LocalMessage::getId, message.getId())
                .eq(LocalMessage::getStatus, LocalMessageStatusEnum.INIT.getStatus()));
        LocalMessageFailureClassification classification = failureClassifier.classify(errorMessage);
        recordFailureCompensationLog(message, retryDecision, classification);
    }

    /**
     * 构建失败后的重试决策。
     * 该决策只描述状态流转，不直接写库，便于主流程清晰表达“失败后如何重试或转死信”。
     *
     * @param message 本地消息
     * @param now 当前时间
     * @return 重试决策
     */
    private RetryDecision buildRetryDecision(LocalMessage message, Date now) {
        int nextRetryCount = defaultRetryCount(message.getRetryCount()) + 1;
        Short nextStatus = nextRetryCount >= MAX_RETRY_COUNT
                ? LocalMessageStatusEnum.FAILED.getStatus()
                : LocalMessageStatusEnum.INIT.getStatus();
        Date nextRetryTime = new Date(now.getTime() + calculateRetryDelayMillis(nextRetryCount));
        return new RetryDecision(nextRetryCount, nextStatus, nextRetryTime);
    }

    /**
     * 写入失败补偿日志。
     * 补偿日志失败不能反向影响本地消息状态更新，否则会导致 relay 重复处理同一条已失败消息。
     *
     * @param message 本地消息
     * @param retryDecision 重试决策
     * @param classification 失败分类
     */
    private void recordFailureCompensationLog(LocalMessage message, RetryDecision retryDecision,
                                              LocalMessageFailureClassification classification) {
        message.setRetryCount(retryDecision.nextRetryCount());
        try {
            compensationLogService.recordFailure(message, classification.reason(), retryDecision.deadLetter(),
                    classification.summary());
        } catch (Exception e) {
            log.error("写补偿日志失败，messageId={}", message.getId(), e);
        }
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
     * 按 UTF-8 字节上限截断时不能直接截 byte[]，否则中文等多字节字符被切半后会出现乱码替换符。
     *
     * @param errorMessage 错误信息
     * @return 截断后的错误信息
     */
    private String truncateError(String errorMessage) {
        return LocalMessageErrorFormatter.truncateUtf8(errorMessage, MAX_ERROR_BYTES);
    }

    /**
     * 本地消息失败后的重试决策。
     *
     * @param nextRetryCount 下一次重试次数
     * @param nextStatus 下一状态
     * @param nextRetryTime 下一次可重试时间
     */
    private record RetryDecision(int nextRetryCount, Short nextStatus, Date nextRetryTime) {

        /**
         * 判断是否已进入死信状态。
         *
         * @return true=已达到最大重试次数
         */
        private boolean deadLetter() {
            return LocalMessageStatusEnum.FAILED.getStatus().equals(nextStatus);
        }
    }
}
