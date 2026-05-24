package com.wayn.domain.trade.support.payment;

import com.wayn.domain.api.trade.entity.OrderGoods;
import com.wayn.domain.trade.outbox.LocalMessageCreateCommand;
import com.wayn.domain.trade.outbox.LocalMessageService;
import com.wayn.domain.api.outbox.service.LocalMessageTopics;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.domain.api.trade.service.IOrderGoodsService;
import com.wayn.domain.inventory.support.OrderStockSupport;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wayn.domain.api.outbox.entity.LocalMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentPostActionSupportTest {

    /**
     * 验证支付后置动作只写入本地消息，不在回调线程直接查询订单商品。
     */
    @Test
    void handleOrderPaidSavesLocalMessage() {
        IOrderGoodsService orderGoodsService = mock(IOrderGoodsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        LocalMessageService localMessageService = mock(LocalMessageService.class);
        OrderStockSupport orderStockSupport = mock(OrderStockSupport.class);
        PaymentPostActionSupport support = new PaymentPostActionSupport(orderGoodsService, goodsService,
                localMessageService, orderStockSupport);

        support.handleOrderPaid(1L);

        ArgumentCaptor<LocalMessageCreateCommand> captor = ArgumentCaptor.forClass(LocalMessageCreateCommand.class);
        verify(localMessageService).saveMessage(captor.capture());
        LocalMessageCreateCommand command = captor.getValue();
        assertEquals("ORDER_PAID_POST_ACTION:1", command.messageKey());
        assertEquals(LocalMessageTopics.ORDER_PAID_POST_ACTION, command.topic());
        assertEquals("1", command.bizId());
    }

    /**
     * 验证本地消息处理器会更新订单商品对应的虚拟销量。
     */
    @Test
    void localMessageHandlerUpdatesVirtualSales() {
        IOrderGoodsService orderGoodsService = mock(IOrderGoodsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        LocalMessageService localMessageService = mock(LocalMessageService.class);
        OrderStockSupport orderStockSupport = mock(OrderStockSupport.class);
        PaymentPostActionSupport support = new PaymentPostActionSupport(orderGoodsService, goodsService,
                localMessageService, orderStockSupport);
        OrderGoods orderGoods = new OrderGoods();
        orderGoods.setGoodsId(10L);
        orderGoods.setNumber(3);
        when(orderGoodsService.list(org.mockito.ArgumentMatchers.<Wrapper<OrderGoods>>any())).thenReturn(List.of(orderGoods));
        LocalMessage message = new LocalMessage();
        message.setTopic(LocalMessageTopics.ORDER_PAID_POST_ACTION);
        message.setPayload("{\"orderId\":1}");

        support.handle(message);

        verify(orderStockSupport).confirmFrozenStockByOrderId(1L);
        verify(goodsService).updateVirtualSales(10L, 3);
    }

    /**
     * 验证本地消息 payload 缺少订单 ID 时会失败，交由本地消息 relay 记录失败并重试。
     */
    @Test
    void localMessageHandlerRejectsPayloadWithoutOrderId() {
        IOrderGoodsService orderGoodsService = mock(IOrderGoodsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        LocalMessageService localMessageService = mock(LocalMessageService.class);
        OrderStockSupport orderStockSupport = mock(OrderStockSupport.class);
        PaymentPostActionSupport support = new PaymentPostActionSupport(orderGoodsService, goodsService,
                localMessageService, orderStockSupport);
        LocalMessage message = new LocalMessage();
        message.setTopic(LocalMessageTopics.ORDER_PAID_POST_ACTION);
        message.setPayload("{}");

        assertThrows(IllegalArgumentException.class, () -> support.handle(message));

        verify(orderStockSupport, never()).confirmFrozenStockByOrderId(org.mockito.ArgumentMatchers.anyLong());
        verify(goodsService, never()).updateVirtualSales(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyInt());
    }
}
