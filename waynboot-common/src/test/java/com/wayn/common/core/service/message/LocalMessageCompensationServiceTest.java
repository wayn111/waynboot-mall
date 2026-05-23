package com.wayn.common.core.service.message;

import com.wayn.common.core.entity.message.LocalMessage;
import com.wayn.common.core.enums.LocalMessageStatusEnum;
import com.wayn.common.core.mapper.message.LocalMessageMapper;
import com.wayn.common.core.service.shop.support.common.MybatisPlusTableInfoTestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalMessageCompensationServiceTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(LocalMessage.class);
    }

    @Mock
    private LocalMessageMapper mapper;
    @Mock
    private LocalMessageCompensationLogService logService;
    private LocalMessageCompensationService service;

    /**
     * 初始化本地消息补偿服务。
     * 单测统一复用 mock，避免每个用例重复创建依赖对象。
     */
    @BeforeEach
    void setUp() {
        service = new LocalMessageCompensationService(mapper, logService);
    }

    /**
     * 查询失败消息时应使用失败状态和安全 limit。
     */
    @Test
    void listFailedMessagesUsesFailedStatusAndSafeLimit() {
        LocalMessage message = newMessage(1L);
        when(mapper.selectList(any())).thenReturn(List.of(message));

        List<LocalMessage> result = service.listFailedMessages(0);

        assertEquals(1, result.size());
        verify(mapper).selectList(any());
    }

    /**
     * Mapper 返回空结果时，查询失败消息也必须返回空列表而不是 null。
     * 管理端页面和定时任务可以直接遍历结果，不需要在每个调用点重复做空指针防御。
     */
    @Test
    void listFailedMessagesReturnsEmptyListWhenMapperReturnsNull() {
        when(mapper.selectList(any())).thenReturn(null);

        List<LocalMessage> result = service.listFailedMessages(10);

        assertTrue(result.isEmpty());
        verify(mapper).selectList(any());
    }

    /**
     * 人工重投成功时应将 FAILED 消息重置为 INIT 并写入重投日志。
     */
    @Test
    void retryFailedMessageResetsMessageToInit() {
        LocalMessage message = newMessage(1L);
        when(mapper.selectById(1L)).thenReturn(message);
        when(mapper.update(isNull(), any())).thenReturn(1);

        boolean retried = service.retryFailedMessage(1L, "admin");

        assertTrue(retried);
        verify(mapper).update(isNull(), any());
        verify(logService).recordManualRetry(message, "admin");
    }

    /**
     * 人工重投操作人应去除首尾空白后再进入日志。
     * 运营日志会用于后续检索和审计，保留空白会导致同一操作人被统计成多个值。
     */
    @Test
    void retryFailedMessageTrimsOperatorBeforeRecordingManualRetryLog() {
        LocalMessage message = newMessage(1L);
        when(mapper.selectById(1L)).thenReturn(message);
        when(mapper.update(isNull(), any())).thenReturn(1);

        boolean retried = service.retryFailedMessage(1L, " admin ");

        assertTrue(retried);
        verify(logService).recordManualRetry(message, "admin");
    }

    /**
     * 条件更新失败时不能记录人工重投日志。
     * 该场景表示消息已经不在 FAILED 状态，记录日志会误导运营判断。
     */
    @Test
    void retryFailedMessageDoesNotRecordManualRetryWhenResetFails() {
        LocalMessage message = newMessage(1L);
        when(mapper.selectById(1L)).thenReturn(message);
        when(mapper.update(isNull(), any())).thenReturn(0);

        boolean retried = service.retryFailedMessage(1L, "admin");

        assertFalse(retried);
        verify(logService, never()).recordManualRetry(any(), any());
    }

    /**
     * 空消息 ID 不能进入人工重投流程。
     * 运营入口直接返回失败，避免生成 id is null 的更新条件或误导性的补偿日志。
     */
    @Test
    void retryFailedMessageRejectsNullMessageId() {
        boolean retried = service.retryFailedMessage(null, "admin");

        assertFalse(retried);
        verifyNoInteractions(mapper, logService);
    }

    /**
     * 统计失败消息数量时应返回补偿监控指标。
     */
    @Test
    void countFailedMessagesBuildsCompensationMetric() {
        when(mapper.selectCount(any())).thenReturn(3L);

        LocalMessageCompensationMetric metric = service.countFailedMessages();

        assertEquals(3L, metric.getFailedCount());
        verify(mapper).selectCount(any());
    }

    /**
     * 构建失败消息测试数据。
     *
     * @param messageId 本地消息 ID
     * @return 失败本地消息
     */
    private LocalMessage newMessage(Long messageId) {
        LocalMessage message = new LocalMessage();
        message.setId(messageId);
        message.setStatus(LocalMessageStatusEnum.FAILED.getStatus());
        return message;
    }
}
