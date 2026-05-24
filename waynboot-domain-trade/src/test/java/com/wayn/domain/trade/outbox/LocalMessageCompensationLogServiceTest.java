package com.wayn.domain.trade.outbox;

import com.wayn.domain.api.outbox.entity.LocalMessage;
import com.wayn.domain.api.outbox.entity.LocalMessageCompensationLog;
import com.wayn.domain.api.outbox.enums.LocalMessageFailureReasonEnum;
import com.wayn.domain.api.outbox.mapper.LocalMessageCompensationLogMapper;
import com.wayn.domain.api.common.MybatisPlusTableInfoTestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalMessageCompensationLogServiceTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(LocalMessageCompensationLog.class);
    }

    @Mock
    private LocalMessageCompensationLogMapper mapper;
    private LocalMessageCompensationLogService service;

    /**
     * 初始化补偿日志服务。
     * 测试只验证日志字段拼装，不连接数据库。
     */
    @BeforeEach
    void setUp() {
        service = new LocalMessageCompensationLogService(mapper);
    }

    /**
     * 普通失败日志应写入失败分类、动作类型和基础消息字段。
     */
    @Test
    void recordFailureWritesClassifiedFailureLog() {
        LocalMessage message = buildMessage();

        service.recordFailure(message, LocalMessageFailureReasonEnum.RABBIT_UNAVAILABLE, false,
                "rabbit down");

        LocalMessageCompensationLog log = captureInsertedLog();
        assertEquals(1L, log.getMessageId());
        assertEquals(LocalMessageFailureReasonEnum.RABBIT_UNAVAILABLE.getReason(), log.getFailureReason());
        assertEquals("FAILURE", log.getActionType());
        assertFalse(log.getDeadLetter());
        assertNotNull(log.getCreateTime());
    }

    /**
     * 达到最大重试次数的失败消息应记录为死信动作。
     */
    @Test
    void recordFailureWritesDeadLetterActionWhenDeadLetterIsTrue() {
        LocalMessage message = buildMessage();

        service.recordFailure(message, LocalMessageFailureReasonEnum.RABBIT_UNAVAILABLE, true,
                "rabbit down");

        LocalMessageCompensationLog log = captureInsertedLog();
        assertEquals("DEAD_LETTER", log.getActionType());
        assertEquals(true, log.getDeadLetter());
    }

    /**
     * 失败原因为空时应归类为 UNKNOWN。
     * relay 捕获到未知异常或上游分类失败时，补偿日志仍需要落库，避免丢失排查证据。
     */
    @Test
    void recordFailureUsesUnknownWhenFailureReasonIsNull() {
        LocalMessage message = buildMessage();

        service.recordFailure(message, null, false, "unknown error");

        LocalMessageCompensationLog log = captureInsertedLog();
        assertEquals(LocalMessageFailureReasonEnum.UNKNOWN.getReason(), log.getFailureReason());
    }

    /**
     * 自动失败日志遇到空白备注时应归一为空串。
     * 补偿后台按 remark 展示和筛选，保留空格会让运营误以为存在有效错误摘要。
     */
    @Test
    void recordFailureNormalizesBlankRemarkToEmptyText() {
        LocalMessage message = buildMessage();

        service.recordFailure(message, LocalMessageFailureReasonEnum.UNKNOWN, false, "   ");

        LocalMessageCompensationLog log = captureInsertedLog();
        assertEquals("", log.getRemark());
    }

    /**
     * 自动失败日志遇到空本地消息时不写入。
     * relay 异常链路可能只拿到异常本身，缺少消息上下文时应避免写入无法定位业务的脏日志。
     */
    @Test
    void recordFailureSkipsNullMessage() {
        service.recordFailure(null, LocalMessageFailureReasonEnum.UNKNOWN, false, "missing message");

        verify(mapper, never()).insert(any(LocalMessageCompensationLog.class));
    }

    /**
     * 本地消息为空时不写补偿日志。
     * 缺失消息无法形成有效业务定位字段，写入半截日志会误导运营排查。
     */
    @Test
    void recordManualRetrySkipsNullMessage() {
        service.recordManualRetry(null, "admin");

        verify(mapper, never()).insert(any(LocalMessageCompensationLog.class));
    }

    /**
     * 人工重投日志应记录默认操作者和固定备注。
     */
    @Test
    void recordManualRetryWritesManualRetryLogWithDefaultOperator() {
        LocalMessage message = buildMessage();

        service.recordManualRetry(message, "");

        LocalMessageCompensationLog log = captureInsertedLog();
        assertEquals("MANUAL_RETRY", log.getActionType());
        assertEquals("system", log.getOperator());
        assertEquals("人工重投失败本地消息", log.getRemark());
        assertFalse(log.getDeadLetter());
    }

    /**
     * 捕获插入的补偿日志。
     *
     * @return 补偿日志
     */
    private LocalMessageCompensationLog captureInsertedLog() {
        ArgumentCaptor<LocalMessageCompensationLog> captor =
                ArgumentCaptor.forClass(LocalMessageCompensationLog.class);
        verify(mapper).insert(captor.capture());
        return captor.getValue();
    }

    /**
     * 构建补偿日志测试消息。
     *
     * @return 本地消息
     */
    private LocalMessage buildMessage() {
        LocalMessage message = new LocalMessage();
        message.setId(1L);
        message.setMessageKey("ORDER:1");
        message.setTopic("ORDER_SUBMIT");
        message.setBizType("ORDER");
        message.setBizId("1");
        message.setRetryCount(2);
        return message;
    }
}
