package com.wayn.common.core.service.message;

import com.wayn.common.core.entity.message.LocalMessage;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalMessageRelaySupportTest {

    /**
     * 验证普通 RabbitMQ 本地消息投递成功后会标记为 SENT。
     */
    @Test
    void relayPendingMessagesMarksMessageSentWhenRabbitSendSucceeds() {
        LocalMessageService localMessageService = mock(LocalMessageService.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitTemplate delayRabbitTemplate = mock(RabbitTemplate.class);
        LocalMessageRelaySupport relaySupport = new LocalMessageRelaySupport(localMessageService,
                rabbitTemplate, delayRabbitTemplate, List.of());
        LocalMessage message = buildRabbitMessage();
        when(localMessageService.listPendingMessages(10)).thenReturn(List.of(message));

        relaySupport.relayPendingMessages(10);

        verify(rabbitTemplate).convertAndSend(eq("order_direct_exchange"), eq("order_direct_routing"),
                any(Message.class), any(CorrelationData.class));
        verify(localMessageService).markSent(1L);
        verify(localMessageService, never()).markFailed(eq(message), any());
    }

    /**
     * 验证 RabbitMQ 投递异常时会记录失败并等待后续重试。
     */
    @Test
    void relayPendingMessagesMarksMessageFailedWhenRabbitSendThrows() {
        LocalMessageService localMessageService = mock(LocalMessageService.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitTemplate delayRabbitTemplate = mock(RabbitTemplate.class);
        LocalMessageRelaySupport relaySupport = new LocalMessageRelaySupport(localMessageService,
                rabbitTemplate, delayRabbitTemplate, List.of());
        LocalMessage message = buildRabbitMessage();
        when(localMessageService.listPendingMessages(10)).thenReturn(List.of(message));
        doThrow(new RuntimeException("rabbit down")).when(rabbitTemplate)
                .convertAndSend(eq("order_direct_exchange"), eq("order_direct_routing"),
                        any(Message.class), any(CorrelationData.class));

        relaySupport.relayPendingMessages(10);

        verify(localMessageService).markFailed(message, "rabbit down");
        verify(localMessageService, never()).markSent(1L);
    }

    /**
     * 验证本地业务消息会交给匹配的本地处理器执行。
     */
    @Test
    void relayPendingMessagesDispatchesLocalHandler() {
        LocalMessageService localMessageService = mock(LocalMessageService.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitTemplate delayRabbitTemplate = mock(RabbitTemplate.class);
        LocalMessageHandler handler = mock(LocalMessageHandler.class);
        LocalMessageRelaySupport relaySupport = new LocalMessageRelaySupport(localMessageService,
                rabbitTemplate, delayRabbitTemplate, List.of(handler));
        LocalMessage message = new LocalMessage();
        message.setId(2L);
        message.setTopic("ORDER_PAID_POST_ACTION");
        message.setPayload("{\"orderId\":10}");
        when(localMessageService.listPendingMessages(10)).thenReturn(List.of(message));
        when(handler.supports("ORDER_PAID_POST_ACTION")).thenReturn(true);

        relaySupport.relayPendingMessages(10);

        verify(handler).handle(message);
        verify(localMessageService).markSent(2L);
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class),
                any(Message.class), any(CorrelationData.class));
    }

    private LocalMessage buildRabbitMessage() {
        LocalMessage message = new LocalMessage();
        message.setId(1L);
        message.setMessageKey("ORDER_SUBMIT:1001");
        message.setTopic("ORDER_SUBMIT");
        message.setExchangeName("order_direct_exchange");
        message.setRoutingKey("order_direct_routing");
        message.setPayload("{\"orderSn\":\"1001\"}");
        return message;
    }
}
