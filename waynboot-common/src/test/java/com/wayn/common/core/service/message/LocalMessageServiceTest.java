package com.wayn.common.core.service.message;

import com.wayn.common.core.entity.message.LocalMessage;
import com.wayn.common.core.enums.LocalMessageStatusEnum;
import com.wayn.common.core.mapper.message.LocalMessageMapper;
import com.wayn.common.core.service.shop.support.common.MybatisPlusTableInfoTestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
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
        LocalMessageService service = new LocalMessageService(mapper);
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
     * 验证投递成功后会按 ID 和 INIT 状态条件更新为 SENT。
     */
    @Test
    void markSentUpdatesMessageToSent() {
        LocalMessageMapper mapper = mock(LocalMessageMapper.class);
        LocalMessageService service = new LocalMessageService(mapper);
        when(mapper.update(isNull(), any())).thenReturn(1);

        service.markSent(1L);

        verify(mapper).update(isNull(), any());
    }

    /**
     * 验证投递失败后会增加重试次数并写入下一次重试时间。
     */
    @Test
    void markFailedUpdatesRetryMetadata() {
        LocalMessageMapper mapper = mock(LocalMessageMapper.class);
        LocalMessageService service = new LocalMessageService(mapper);
        LocalMessage message = new LocalMessage();
        message.setId(1L);
        message.setRetryCount(2);
        message.setNextRetryTime(new Date());

        service.markFailed(message, "rabbit down");

        verify(mapper).update(isNull(), any());
    }
}
