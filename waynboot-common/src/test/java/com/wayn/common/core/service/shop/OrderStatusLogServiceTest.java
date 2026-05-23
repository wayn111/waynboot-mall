package com.wayn.common.core.service.shop;

import com.wayn.common.core.entity.shop.OrderStatusLog;
import com.wayn.common.core.enums.OrderStatusChangeTypeEnum;
import com.wayn.common.core.mapper.shop.OrderStatusLogMapper;
import com.wayn.common.core.service.shop.support.common.MybatisPlusTableInfoTestHelper;
import com.wayn.util.enums.OrderStatusEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderStatusLogServiceTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(OrderStatusLog.class);
    }

    @Test
    void recordSuccessMapsCommandToStatusLogEntity() {
        OrderStatusLogMapper mapper = mock(OrderStatusLogMapper.class);
        OrderStatusLogService service = new OrderStatusLogService(mapper);
        when(mapper.insert(any(OrderStatusLog.class))).thenReturn(1);

        service.recordSuccess(OrderStatusChangeCommand.builder()
                .orderId(1L)
                .orderSn("order-1")
                .sourceStatus(OrderStatusEnum.STATUS_CREATE)
                .targetStatus(OrderStatusEnum.STATUS_PAY)
                .changeType(OrderStatusChangeTypeEnum.PAY_CALLBACK)
                .operatorType("SYSTEM")
                .operatorId("wechat")
                .remark("微信支付回调")
                .build());

        org.mockito.ArgumentCaptor<OrderStatusLog> captor = org.mockito.ArgumentCaptor.forClass(OrderStatusLog.class);
        verify(mapper).insert(captor.capture());
        OrderStatusLog log = captor.getValue();
        assertEquals(1L, log.getOrderId());
        assertEquals("order-1", log.getOrderSn());
        assertEquals(OrderStatusEnum.STATUS_CREATE.getStatus(), log.getSourceStatus());
        assertEquals(OrderStatusEnum.STATUS_PAY.getStatus(), log.getTargetStatus());
        assertEquals(OrderStatusChangeTypeEnum.PAY_CALLBACK.getCode(), log.getChangeType());
        assertEquals("SYSTEM", log.getOperatorType());
        assertEquals("wechat", log.getOperatorId());
        assertEquals(Boolean.TRUE, log.getSuccess());
        assertEquals("微信支付回调", log.getRemark());
    }

    @Test
    void recordFailureDoesNotInterruptMainTradeFlowWhenInsertFails() {
        OrderStatusLogMapper mapper = mock(OrderStatusLogMapper.class);
        OrderStatusLogService service = new OrderStatusLogService(mapper);
        when(mapper.insert(any(OrderStatusLog.class))).thenThrow(new IllegalStateException("db unavailable"));

        assertDoesNotThrow(() -> service.recordFailure(OrderStatusChangeCommand.builder()
                .orderId(2L)
                .orderSn("order-2")
                .sourceStatus(OrderStatusEnum.STATUS_CREATE)
                .targetStatus(OrderStatusEnum.STATUS_CANCEL)
                .changeType(OrderStatusChangeTypeEnum.USER_CANCEL)
                .operatorType("USER")
                .operatorId("10001")
                .remark("用户取消")
                .build(), "状态条件更新失败"));

        verify(mapper).insert(any(OrderStatusLog.class));
    }

    @Test
    void recordSuccessSkipsNullCommandWithoutTouchingDatabase() {
        OrderStatusLogMapper mapper = mock(OrderStatusLogMapper.class);
        OrderStatusLogService service = new OrderStatusLogService(mapper);

        assertDoesNotThrow(() -> service.recordSuccess(null));

        verify(mapper, never()).insert(any(OrderStatusLog.class));
    }

    @Test
    void recordFailureNormalizesBlankFailReasonAndRemarkToEmptyText() {
        OrderStatusLogMapper mapper = mock(OrderStatusLogMapper.class);
        OrderStatusLogService service = new OrderStatusLogService(mapper);
        when(mapper.insert(any(OrderStatusLog.class))).thenReturn(1);

        service.recordFailure(OrderStatusChangeCommand.builder()
                .orderId(3L)
                .orderSn("order-3")
                .sourceStatus(OrderStatusEnum.STATUS_CREATE)
                .targetStatus(OrderStatusEnum.STATUS_CANCEL)
                .changeType(OrderStatusChangeTypeEnum.AUTO_CANCEL)
                .operatorType("SYSTEM")
                .operatorId("scheduler")
                .remark("   ")
                .build(), "   ");

        org.mockito.ArgumentCaptor<OrderStatusLog> captor = org.mockito.ArgumentCaptor.forClass(OrderStatusLog.class);
        verify(mapper).insert(captor.capture());
        OrderStatusLog log = captor.getValue();
        assertEquals("", log.getFailReason());
        assertEquals("", log.getRemark());
    }
}
