package com.wayn.common.core.service.message;

import com.wayn.common.core.entity.message.LocalMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalMessageRelaySupportTest {

    @Mock
    private LocalMessageService localMessageService;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private RabbitTemplate delayRabbitTemplate;

    /**
     * 验证普通 RabbitMQ 本地消息投递成功后会标记为 SENT。
     */
    @Test
    void relayPendingMessagesMarksMessageSentWhenRabbitSendSucceeds() {
        LocalMessageRelaySupport relaySupport = newRelaySupport();
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
        LocalMessageRelaySupport relaySupport = newRelaySupport();
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
     * 验证延迟本地消息会走延迟 RabbitTemplate，并设置延迟属性。
     */
    @Test
    void relayPendingMessagesUsesDelayRabbitTemplateWhenDelayMillisPresent() {
        LocalMessageRelaySupport relaySupport = newRelaySupport();
        LocalMessage message = buildRabbitMessage();
        message.setDelayMillis(30000);
        when(localMessageService.listPendingMessages(10)).thenReturn(List.of(message));

        relaySupport.relayPendingMessages(10);

        verify(delayRabbitTemplate).convertAndSend(eq("order_direct_exchange"), eq("order_direct_routing"),
                any(Message.class), any(MessagePostProcessor.class));
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class),
                any(Message.class), any(CorrelationData.class));
        verify(localMessageService).markSent(1L);
    }

    /**
     * 验证本地业务消息会交给匹配的本地处理器执行。
     */
    @Test
    void relayPendingMessagesDispatchesLocalHandler() {
        LocalMessageHandler handler = mock(LocalMessageHandler.class);
        LocalMessageRelaySupport relaySupport = new LocalMessageRelaySupport(localMessageService,
                rabbitTemplate, delayRabbitTemplate, List.of(handler));
        LocalMessage message = buildLocalMessage();
        when(localMessageService.listPendingMessages(10)).thenReturn(List.of(message));
        when(handler.supports("ORDER_PAID_POST_ACTION")).thenReturn(true);

        relaySupport.relayPendingMessages(10);

        verify(handler).handle(message);
        verify(localMessageService).markSent(2L);
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class),
                any(Message.class), any(CorrelationData.class));
    }

    /**
     * 待投递列表中出现空消息时应跳过并继续处理后续消息。
     * relay 批处理不能因为一条脏数据中断整批，否则会放大本地消息积压。
     */
    @Test
    void relayPendingMessagesSkipsNullMessageAndContinuesBatch() {
        LocalMessageRelaySupport relaySupport = newRelaySupport();
        LocalMessage message = buildRabbitMessage();
        when(localMessageService.listPendingMessages(10)).thenReturn(Arrays.asList(null, message));

        relaySupport.relayPendingMessages(10);

        verify(rabbitTemplate).convertAndSend(eq("order_direct_exchange"), eq("order_direct_routing"),
                any(Message.class), any(CorrelationData.class));
        verify(localMessageService).markSent(1L);
        verify(localMessageService, never()).markFailed(eq(null), any());
    }

    /**
     * 待投递列表为 null 时应按空批次处理。
     * relay 是定时批处理入口，不能把下游异常空结果放大成 NPE 造成任务失败。
     */
    @Test
    void relayPendingMessagesTreatsNullPendingListAsEmptyBatch() {
        LocalMessageRelaySupport relaySupport = newRelaySupport();
        when(localMessageService.listPendingMessages(10)).thenReturn(null);

        relaySupport.relayPendingMessages(10);

        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class),
                any(Message.class), any(CorrelationData.class));
        verify(localMessageService, never()).markSent(any());
        verify(localMessageService, never()).markFailed(any(), any());
    }

    /**
     * 构建默认 relay 服务。
     *
     * @return 本地消息 relay 服务
     */
    private LocalMessageRelaySupport newRelaySupport() {
        return new LocalMessageRelaySupport(localMessageService, rabbitTemplate, delayRabbitTemplate, List.of());
    }

    /**
     * 构建 RabbitMQ 本地消息。
     *
     * @return RabbitMQ 本地消息
     */
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

    /**
     * 构建本地 handler 消息。
     *
     * @return 本地 handler 消息
     */
    private LocalMessage buildLocalMessage() {
        LocalMessage message = new LocalMessage();
        message.setId(2L);
        message.setTopic("ORDER_PAID_POST_ACTION");
        message.setPayload("{\"orderId\":10}");
        return message;
    }
}
