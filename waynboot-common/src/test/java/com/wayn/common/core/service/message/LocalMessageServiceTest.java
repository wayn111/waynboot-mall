package com.wayn.common.core.service.message;

import com.wayn.common.core.entity.message.LocalMessage;
import com.wayn.common.core.enums.LocalMessageStatusEnum;
import com.wayn.common.core.mapper.message.LocalMessageMapper;
import com.wayn.common.core.service.shop.support.common.MybatisPlusTableInfoTestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalMessageServiceTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(LocalMessage.class);
    }

    /**
     * 验证保存本地消息时会写入 INIT 状态、重试次数和首次可投递时间。
     */
    @Test
    void saveMessageInsertsInitialMessage() {
        LocalMessageMapper mapper = mock(LocalMessageMapper.class);
        LocalMessageService service = new LocalMessageService(mapper, new LocalMessageFailureClassifier(),
                mock(LocalMessageCompensationLogService.class));
        LocalMessageCreateCommand command = LocalMessageCreateCommand.builder()
                .messageKey("ORDER_SUBMIT:1001")
                .topic("ORDER_SUBMIT")
                .bizType("ORDER")
                .bizId("1001")
                .exchangeName("order_direct_exchange")
                .routingKey("order_direct_routing")
                .payload("{\"orderSn\":\"1001\"}")
                .build();
        when(mapper.insert(any(LocalMessage.class))).thenReturn(1);

        service.saveMessage(command);

        ArgumentCaptor<LocalMessage> captor = ArgumentCaptor.forClass(LocalMessage.class);
        verify(mapper).insert(captor.capture());
        LocalMessage message = captor.getValue();
        assertEquals("ORDER_SUBMIT:1001", message.getMessageKey());
        assertEquals(LocalMessageStatusEnum.INIT.getStatus(), message.getStatus());
        assertEquals(0, message.getRetryCount());
        assertNotNull(message.getNextRetryTime());
        assertNotNull(message.getCreateTime());
        assertNotNull(message.getUpdateTime());
    }

    /**
     * 空创建命令不写入本地消息。
     * 本地消息必须带有业务幂等键，缺少命令上下文时写库只会制造不可补偿的脏数据。
     */
    @Test
    void saveMessageSkipsNullCommand() {
        LocalMessageMapper mapper = mock(LocalMessageMapper.class);
        LocalMessageService service = new LocalMessageService(mapper, new LocalMessageFailureClassifier(),
                mock(LocalMessageCompensationLogService.class));

        service.saveMessage(null);

        verify(mapper, never()).insert(any(LocalMessage.class));
    }

    /**
     * messageKey 为空时不写入本地消息。
     * messageKey 是 outbox 幂等和补偿定位的核心字段，缺失时不能进入 relay 链路。
     */
    @Test
    void saveMessageSkipsBlankMessageKey() {
        LocalMessageMapper mapper = mock(LocalMessageMapper.class);
        LocalMessageService service = new LocalMessageService(mapper, new LocalMessageFailureClassifier(),
                mock(LocalMessageCompensationLogService.class));

        service.saveMessage(LocalMessageCreateCommand.builder()
                .messageKey(" ")
                .topic("ORDER_SUBMIT")
                .bizType("ORDER")
                .bizId("1001")
                .payload("{}")
                .build());

        verify(mapper, never()).insert(any(LocalMessage.class));
    }

    /**
     * 验证投递成功后会按 ID 和 INIT 状态条件更新为 SENT。
     */
    @Test
    void markSentUpdatesMessageToSent() {
        LocalMessageMapper mapper = mock(LocalMessageMapper.class);
        LocalMessageService service = new LocalMessageService(mapper, new LocalMessageFailureClassifier(),
                mock(LocalMessageCompensationLogService.class));
        when(mapper.update(isNull(), any())).thenReturn(1);

        service.markSent(1L);

        verify(mapper).update(isNull(), any());
    }

    /**
     * Mapper 返回空结果时，到期待投递消息列表应保持空列表语义。
     * relay 调用方可以直接遍历结果，避免每个批处理入口重复写 null 防御。
     */
    @Test
    void listPendingMessagesReturnsEmptyListWhenMapperReturnsNull() {
        LocalMessageMapper mapper = mock(LocalMessageMapper.class);
        LocalMessageService service = new LocalMessageService(mapper, new LocalMessageFailureClassifier(),
                mock(LocalMessageCompensationLogService.class));
        when(mapper.selectList(any())).thenReturn(null);

        List<LocalMessage> messages = service.listPendingMessages(10);

        assertTrue(messages.isEmpty());
        verify(mapper).selectList(any());
    }

    /**
     * 验证投递失败后会增加重试次数并写入下一次重试时间。
     */
    @Test
    void markFailedUpdatesRetryMetadata() {
        LocalMessageMapper mapper = mock(LocalMessageMapper.class);
        LocalMessageCompensationLogService logService = mock(LocalMessageCompensationLogService.class);
        LocalMessageService service = new LocalMessageService(mapper, new LocalMessageFailureClassifier(), logService);
        LocalMessage message = new LocalMessage();
        message.setId(1L);
        message.setRetryCount(2);
        message.setNextRetryTime(new Date());

        service.markFailed(message, "rabbit down");

        verify(mapper).update(isNull(), any());
        verify(logService).recordFailure(any(), any(), any(Boolean.class), any());
    }

    /**
     * 验证达到最大重试次数后补偿日志会标记为死信。
     */
    @Test
    void markFailedMarksCompensationLogAsDeadLetterWhenMaxRetryReached() {
        LocalMessageMapper mapper = mock(LocalMessageMapper.class);
        LocalMessageCompensationLogService logService = mock(LocalMessageCompensationLogService.class);
        LocalMessageService service = new LocalMessageService(mapper, new LocalMessageFailureClassifier(), logService);
        LocalMessage message = new LocalMessage();
        message.setId(1L);
        message.setRetryCount(4);

        service.markFailed(message, "rabbit down");

        ArgumentCaptor<Boolean> deadLetterCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(logService).recordFailure(any(), any(), deadLetterCaptor.capture(), any());
        assertEquals(Boolean.TRUE, deadLetterCaptor.getValue());
    }

}
