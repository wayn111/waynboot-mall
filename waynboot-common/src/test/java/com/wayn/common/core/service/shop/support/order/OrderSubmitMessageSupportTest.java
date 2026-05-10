package com.wayn.common.core.service.shop.support.order;

import com.wayn.common.config.WaynConfig;
import com.wayn.common.core.service.message.LocalMessageCreateCommand;
import com.wayn.common.core.service.message.LocalMessageService;
import com.wayn.message.core.constant.MQConstants;
import com.wayn.message.core.dto.OrderDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrderSubmitMessageSupportTest {

    /**
     * 验证异步下单入口不再直接投递 MQ，而是写入本地消息表等待 relay 投递。
     */
    @Test
    void sendSubmitMessageSavesLocalMessage() {
        new WaynConfig().setMobileUrl("http://mobile");
        LocalMessageService localMessageService = mock(LocalMessageService.class);
        OrderSubmitMessageSupport support = new OrderSubmitMessageSupport(localMessageService);
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderSn("order-1");

        support.sendSubmitMessage(orderDTO);

        ArgumentCaptor<LocalMessageCreateCommand> captor = ArgumentCaptor.forClass(LocalMessageCreateCommand.class);
        verify(localMessageService).saveMessage(captor.capture());
        LocalMessageCreateCommand command = captor.getValue();
        assertEquals("ORDER_SUBMIT:order-1", command.messageKey());
        assertEquals(MQConstants.ORDER_DIRECT_EXCHANGE, command.exchangeName());
        assertEquals(MQConstants.ORDER_DIRECT_ROUTING, command.routingKey());
        assertTrue(command.payload().contains("/callback/order/submit"));
    }

    /**
     * 验证未支付关单消息会带延迟时间写入本地消息表。
     */
    @Test
    void saveUnpaidDelayMessageSavesDelayedLocalMessage() {
        new WaynConfig().setMobileUrl("http://mobile");
        new WaynConfig().setUnpaidOrderCancelDelayTime(2);
        new WaynConfig().setFreightLimit(new BigDecimal("999"));
        LocalMessageService localMessageService = mock(LocalMessageService.class);
        OrderSubmitMessageSupport support = new OrderSubmitMessageSupport(localMessageService);

        support.saveUnpaidDelayMessage("order-2");

        ArgumentCaptor<LocalMessageCreateCommand> captor = ArgumentCaptor.forClass(LocalMessageCreateCommand.class);
        verify(localMessageService).saveMessage(captor.capture());
        LocalMessageCreateCommand command = captor.getValue();
        assertEquals("ORDER_UNPAID_DELAY:order-2", command.messageKey());
        assertEquals(MQConstants.ORDER_DELAY_EXCHANGE, command.exchangeName());
        assertEquals(MQConstants.ORDER_DELAY_ROUTING, command.routingKey());
        assertTrue(command.delayMillis() > 0);
    }
}
