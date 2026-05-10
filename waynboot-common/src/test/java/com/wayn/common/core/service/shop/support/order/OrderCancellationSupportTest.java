package com.wayn.common.core.service.shop.support.order;

import com.wayn.common.core.service.shop.support.common.MybatisPlusTableInfoTestHelper;

import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.mapper.shop.OrderMapper;
import com.wayn.common.core.service.shop.ShopMemberCouponService;
import com.wayn.common.core.service.shop.support.common.TradeLockSupport;
import com.wayn.util.enums.OrderStatusEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCancellationSupportTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(Order.class);
    }

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderStockSupport orderStockSupport;
    @Mock
    private ShopMemberCouponService shopMemberCouponService;
    @Mock
    private TradeLockSupport tradeLockSupport;

    @Test
    void cancelSkipsCompensationWhenConditionalUpdateAffectsNoRows() {
        OrderCancellationSupport support = new OrderCancellationSupport(orderMapper, orderStockSupport,
                shopMemberCouponService, tradeLockSupport, new OrderStateTransitionSupport());
        Order order = new Order();
        order.setId(1L);
        order.setOrderSn("order-cancel-1");
        order.setOrderStatus(OrderStatusEnum.STATUS_CREATE.getStatus());

        when(tradeLockSupport.tryRunWithLock(anyString(), isNull(), any()))
                .thenAnswer(invocation -> {
                    invocation.<Runnable>getArgument(2).run();
                    return true;
                });
        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.update(any(), any())).thenReturn(0);

        support.cancel("order-cancel-1", OrderStatusEnum.STATUS_CANCEL);

        verify(orderStockSupport, never()).releaseFrozenStockByOrderId(anyLong());
        verify(shopMemberCouponService, never()).lambdaUpdate();
    }

    @Test
    void cancelSkipsCompensationWhenOrderIsAlreadyProcessed() {
        OrderCancellationSupport support = new OrderCancellationSupport(orderMapper, orderStockSupport,
                shopMemberCouponService, tradeLockSupport, new OrderStateTransitionSupport());
        Order order = new Order();
        order.setId(2L);
        order.setOrderSn("order-cancel-2");
        order.setOrderStatus(OrderStatusEnum.STATUS_PAY.getStatus());

        when(tradeLockSupport.tryRunWithLock(anyString(), isNull(), any()))
                .thenAnswer(invocation -> {
                    invocation.<Runnable>getArgument(2).run();
                    return true;
                });
        when(orderMapper.selectOne(any())).thenReturn(order);

        support.cancel("order-cancel-2", OrderStatusEnum.STATUS_AUTO_CANCEL);

        verify(orderMapper, never()).update(any(), any());
        verify(orderStockSupport, never()).releaseFrozenStockByOrderId(anyLong());
        verify(shopMemberCouponService, never()).lambdaUpdate();
    }
}
