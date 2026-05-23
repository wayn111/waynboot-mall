package com.wayn.common.core.service.shop.support.admin.order;

import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.mapper.shop.AdminOrderMapper;
import com.wayn.common.core.service.shop.OrderStatusLogService;
import com.wayn.common.core.service.shop.support.common.MybatisPlusTableInfoTestHelper;
import com.wayn.common.core.service.shop.support.order.OrderStateTransitionSupport;
import com.wayn.common.model.request.ShipRequestVO;
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
class AdminOrderShipmentSupportTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(Order.class);
    }

    @Mock
    private AdminOrderMapper adminOrderMapper;
    @Mock
    private OrderStatusLogService orderStatusLogService;

    @Test
    void shipThrowsWhenConditionalUpdateAffectsNoRows() {
        AdminOrderShipmentSupport support = new AdminOrderShipmentSupport(adminOrderMapper,
                new OrderStateTransitionSupport(), orderStatusLogService);
        ShipRequestVO shipRequestVO = new ShipRequestVO();
        shipRequestVO.setOrderId(1L);
        shipRequestVO.setShipChannel("SF");
        shipRequestVO.setShipSn("SF123");
        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus(OrderStatusEnum.STATUS_PAY.getStatus());

        when(adminOrderMapper.selectById(1L)).thenReturn(order);
        when(adminOrderMapper.update(any(Order.class), any())).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> support.ship(shipRequestVO));

        assertEquals(ReturnCodeEnum.ORDER_CANNOT_SHIP_ERROR.getCode(), exception.getCode());
        verify(adminOrderMapper).update(any(Order.class), any());
    }

    @Test
    void shipRecordsStatusLogWhenConditionalUpdateSucceeds() {
        AdminOrderShipmentSupport support = new AdminOrderShipmentSupport(adminOrderMapper,
                new OrderStateTransitionSupport(), orderStatusLogService);
        ShipRequestVO shipRequestVO = new ShipRequestVO();
        shipRequestVO.setOrderId(2L);
        shipRequestVO.setShipChannel("SF");
        shipRequestVO.setShipSn("SF456");
        Order order = new Order();
        order.setId(2L);
        order.setOrderSn("ship-order-2");
        order.setOrderStatus(OrderStatusEnum.STATUS_PAY.getStatus());

        when(adminOrderMapper.selectById(2L)).thenReturn(order);
        when(adminOrderMapper.update(any(Order.class), any())).thenReturn(1);

        support.ship(shipRequestVO);

        verify(orderStatusLogService).recordSuccess(any());
    }
}
