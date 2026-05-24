package com.wayn.domain.trade.support.order.submit.chain;

import com.wayn.domain.api.cart.entity.Cart;
import com.wayn.domain.inventory.support.OrderStockSupport;
import com.wayn.domain.inventory.support.RedisStockPreDeductSupport;
import com.wayn.domain.inventory.support.RedisStockReservation;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.util.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderSubmitStockReduceStepTest {

    @Test
    void executeReleasesRedisReservationAfterMysqlFreezeSucceeds() {
        OrderStockSupport orderStockSupport = mock(OrderStockSupport.class);
        RedisStockPreDeductSupport redisStockPreDeductSupport = mock(RedisStockPreDeductSupport.class);
        RedisStockReservation reservation = mock(RedisStockReservation.class);
        OrderSubmitStockReduceStep step = new OrderSubmitStockReduceStep(orderStockSupport, redisStockPreDeductSupport);
        OrderSubmitChainContext context = buildContext();
        when(redisStockPreDeductSupport.preDeduct("order-1", context.getSubmitContext().checkedGoodsList()))
                .thenReturn(reservation);

        step.execute(context);

        verify(orderStockSupport).freezeStock("order-1", context.getSubmitContext().checkedGoodsList());
        verify(reservation).release();
    }

    @Test
    void executeReleasesRedisReservationWhenMysqlFreezeFails() {
        OrderStockSupport orderStockSupport = mock(OrderStockSupport.class);
        RedisStockPreDeductSupport redisStockPreDeductSupport = mock(RedisStockPreDeductSupport.class);
        RedisStockReservation reservation = mock(RedisStockReservation.class);
        OrderSubmitStockReduceStep step = new OrderSubmitStockReduceStep(orderStockSupport, redisStockPreDeductSupport);
        OrderSubmitChainContext context = buildContext();
        when(redisStockPreDeductSupport.preDeduct("order-1", context.getSubmitContext().checkedGoodsList()))
                .thenReturn(reservation);
        org.mockito.Mockito.doThrow(new BusinessException("mysql stock failed"))
                .when(orderStockSupport).freezeStock("order-1", context.getSubmitContext().checkedGoodsList());

        assertThrows(BusinessException.class, () -> step.execute(context));

        verify(reservation).release();
    }

    private OrderSubmitChainContext buildContext() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderSn("order-1");
        Cart cart = new Cart();
        cart.setGoodsId(10L);
        cart.setProductId(100L);
        cart.setNumber(2);
        OrderSubmitChainContext context = OrderSubmitChainContext.single(orderDTO,
                ignored -> new com.wayn.domain.trade.support.order.OrderSubmitContext(
                        null, List.of(cart), null, null, null, null, null));
        context.buildSubmitContext();
        return context;
    }
}
