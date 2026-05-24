package com.wayn.domain.trade.support.order;

import com.wayn.domain.api.common.MybatisPlusTableInfoTestHelper;

import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.mapper.OrderMapper;
import com.wayn.domain.api.trade.service.IOrderGoodsService;
import com.wayn.domain.api.trade.service.OrderStatusLogService;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderLifecycleSupportTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(Order.class);
    }

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private IOrderGoodsService orderGoodsService;
    @Mock
    private OrderCancellationSupport orderCancellationSupport;
    @Mock
    private OrderStatusLogService orderStatusLogService;

    @Test
    void refundThrowsWhenConditionalUpdateAffectsNoRows() {
        OrderLifecycleSupport support = new OrderLifecycleSupport(orderMapper, orderGoodsService,
                new OrderValidationSupport(), orderCancellationSupport, new OrderStateTransitionSupport(),
                orderStatusLogService);
        Order order = buildOrder(1L, "refund-order", OrderStatusEnum.STATUS_PAY);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.update(any(), any())).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> support.refund(1L));

        assertEquals(ReturnCodeEnum.ERROR.getCode(), exception.getCode());
    }

    @Test
    void confirmThrowsWhenConditionalUpdateAffectsNoRows() {
        OrderLifecycleSupport support = new OrderLifecycleSupport(orderMapper, orderGoodsService,
                new OrderValidationSupport(), orderCancellationSupport, new OrderStateTransitionSupport(),
                orderStatusLogService);
        Order order = buildOrder(2L, "confirm-order", OrderStatusEnum.STATUS_SHIP);
        when(orderMapper.selectById(2L)).thenReturn(order);
        when(orderMapper.update(any(), any())).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> support.confirm(2L));

        assertEquals(ReturnCodeEnum.ERROR.getCode(), exception.getCode());
    }

    @Test
    void deleteThrowsWhenConditionalDeleteAffectsNoRows() {
        OrderLifecycleSupport support = new OrderLifecycleSupport(orderMapper, orderGoodsService,
                new OrderValidationSupport(), orderCancellationSupport, new OrderStateTransitionSupport(),
                orderStatusLogService);
        Order order = buildOrder(3L, "delete-order", OrderStatusEnum.STATUS_CONFIRM);
        when(orderMapper.selectById(3L)).thenReturn(order);
        when(orderMapper.delete(any())).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> support.delete(3L));

        assertEquals(ReturnCodeEnum.ERROR.getCode(), exception.getCode());
    }

    @Test
    void cancelDelegatesToCancellationSupport() {
        OrderLifecycleSupport support = new OrderLifecycleSupport(orderMapper, orderGoodsService,
                new OrderValidationSupport(), orderCancellationSupport, new OrderStateTransitionSupport(),
                orderStatusLogService);
        Order order = buildOrder(4L, "cancel-order", OrderStatusEnum.STATUS_CREATE);
        when(orderMapper.selectById(4L)).thenReturn(order);

        support.cancel(4L);

        verify(orderCancellationSupport).cancel("cancel-order", OrderStatusEnum.STATUS_CANCEL);
    }

    @Test
    void refundRecordsStatusLogWhenConditionalUpdateSucceeds() {
        OrderLifecycleSupport support = new OrderLifecycleSupport(orderMapper, orderGoodsService,
                new OrderValidationSupport(), orderCancellationSupport, new OrderStateTransitionSupport(),
                orderStatusLogService);
        Order order = buildOrder(5L, "refund-success-order", OrderStatusEnum.STATUS_PAY);
        when(orderMapper.selectById(5L)).thenReturn(order);
        when(orderMapper.update(any(), any())).thenReturn(1);

        support.refund(5L);

        verify(orderStatusLogService).recordSuccess(any());
    }

    @Test
    void confirmRecordsStatusLogWhenConditionalUpdateSucceeds() {
        OrderLifecycleSupport support = new OrderLifecycleSupport(orderMapper, orderGoodsService,
                new OrderValidationSupport(), orderCancellationSupport, new OrderStateTransitionSupport(),
                orderStatusLogService);
        Order order = buildOrder(6L, "confirm-success-order", OrderStatusEnum.STATUS_SHIP);
        when(orderMapper.selectById(6L)).thenReturn(order);
        when(orderMapper.update(any(), any())).thenReturn(1);

        support.confirm(6L);

        verify(orderStatusLogService).recordSuccess(any());
    }

    private Order buildOrder(Long id, String orderSn, OrderStatusEnum statusEnum) {
        Order order = new Order();
        order.setId(id);
        order.setOrderSn(orderSn);
        order.setOrderStatus(statusEnum.getStatus());
        return order;
    }
}
