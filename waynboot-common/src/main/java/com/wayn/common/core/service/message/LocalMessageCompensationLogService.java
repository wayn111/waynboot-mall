package com.wayn.common.core.service.message;

import com.wayn.common.core.entity.message.LocalMessage;
import com.wayn.common.core.entity.message.LocalMessageCompensationLog;
import com.wayn.common.core.enums.LocalMessageFailureReasonEnum;
import com.wayn.common.core.mapper.message.LocalMessageCompensationLogMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 本地消息补偿日志服务。
 * 统一记录自动失败、死信和人工重投动作，避免补偿后台和 relay 各自拼装日志字段。
 */
@Service
@AllArgsConstructor
public class LocalMessageCompensationLogService {

    private static final String ACTION_TYPE_FAILURE = "FAILURE";
    private static final String ACTION_TYPE_DEAD_LETTER = "DEAD_LETTER";
    private static final String ACTION_TYPE_MANUAL_RETRY = "MANUAL_RETRY";
    private static final String SYSTEM_OPERATOR = "system";
    private static final String MANUAL_RETRY_REMARK = "人工重投失败本地消息";

    private final LocalMessageCompensationLogMapper compensationLogMapper;

    /**
     * 记录自动投递失败。
     * deadLetter=true 表示消息已达到最大重试次数，需要运营后台或补偿任务进一步处理。
     *
     * @param message 本地消息
     * @param failureReason 失败原因
     * @param deadLetter 是否进入死信
     * @param remark 日志备注
     */
    public void recordFailure(LocalMessage message, LocalMessageFailureReasonEnum failureReason, boolean deadLetter,
                              String remark) {
        if (message == null) {
            // 空消息缺少 messageKey、topic、bizId 等定位字段，写入半截日志反而会干扰补偿排查。
            return;
        }
        LocalMessageCompensationLog log = buildBaseLog(message);
        fillFailureFields(log, failureReason, deadLetter, remark);
        compensationLogMapper.insert(log);
    }

    /**
     * 记录人工重投。
     *
     * @param message 本地消息
     * @param operator 操作者
     */
    public void recordManualRetry(LocalMessage message, String operator) {
        if (message == null) {
            // 人工重投必须能追溯到具体本地消息，缺少消息上下文时直接跳过日志写入。
            return;
        }
        LocalMessageCompensationLog log = buildBaseLog(message);
        fillManualRetryFields(log, operator);
        compensationLogMapper.insert(log);
    }

    /**
     * 填充自动失败日志字段。
     *
     * @param log 补偿日志
     * @param failureReason 失败原因
     * @param deadLetter 是否进入死信
     * @param remark 日志备注
     */
    private void fillFailureFields(LocalMessageCompensationLog log, LocalMessageFailureReasonEnum failureReason,
                                   boolean deadLetter, String remark) {
        LocalMessageFailureReasonEnum safeFailureReason = resolveFailureReason(failureReason);
        log.setActionType(resolveFailureActionType(deadLetter));
        log.setFailureReason(safeFailureReason.getReason());
        log.setDeadLetter(deadLetter);
        log.setOperator(SYSTEM_OPERATOR);
        log.setRemark(normalizeRemark(remark));
    }

    /**
     * 填充人工重投日志字段。
     *
     * @param log 补偿日志
     * @param operator 操作者
     */
    private void fillManualRetryFields(LocalMessageCompensationLog log, String operator) {
        log.setActionType(ACTION_TYPE_MANUAL_RETRY);
        log.setFailureReason("");
        log.setDeadLetter(false);
        log.setOperator(StringUtils.defaultIfBlank(operator, SYSTEM_OPERATOR));
        log.setRemark(MANUAL_RETRY_REMARK);
    }

    /**
     * 解析失败日志动作类型。
     *
     * @param deadLetter 是否进入死信
     * @return 失败动作类型
     */
    private String resolveFailureActionType(boolean deadLetter) {
        return deadLetter ? ACTION_TYPE_DEAD_LETTER : ACTION_TYPE_FAILURE;
    }

    /**
     * 解析失败原因。
     * relay 分类逻辑异常或调用方未传入分类时，统一归入 UNKNOWN，保证补偿日志仍可落库并参与统计。
     *
     * @param failureReason 原始失败原因
     * @return 可写入日志的失败原因
     */
    private LocalMessageFailureReasonEnum resolveFailureReason(LocalMessageFailureReasonEnum failureReason) {
        if (failureReason == null) {
            return LocalMessageFailureReasonEnum.UNKNOWN;
        }
        return failureReason;
    }

    /**
     * 规范化失败备注。
     * 补偿后台会直接展示 remark，空白文本没有排查价值，统一收敛为空串方便筛选和统计。
     *
     * @param remark 原始备注
     * @return 可落库备注
     */
    private String normalizeRemark(String remark) {
        return StringUtils.defaultIfBlank(remark, "");
    }

    /**
     * 构建补偿日志基础字段。
     *
     * @param message 本地消息
     * @return 补偿日志实体
     */
    private LocalMessageCompensationLog buildBaseLog(LocalMessage message) {
        Date now = new Date();
        LocalMessageCompensationLog log = new LocalMessageCompensationLog();
        log.setMessageId(message.getId());
        log.setMessageKey(message.getMessageKey());
        log.setTopic(message.getTopic());
        log.setBizType(message.getBizType());
        log.setBizId(message.getBizId());
        log.setRetryCount(message.getRetryCount());
        log.setCreateTime(now);
        log.setUpdateTime(now);
        return log;
    }
}
