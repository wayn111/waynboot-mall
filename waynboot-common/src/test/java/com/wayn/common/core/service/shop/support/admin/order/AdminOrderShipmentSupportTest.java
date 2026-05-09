package com.wayn.common.core.service.shop.support.admin.order;

import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.mapper.shop.AdminOrderMapper;
import com.wayn.common.core.service.shop.support.order.OrderStateTransitionSupport;
import com.wayn.common.model.request.ShipRequestVO;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
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

    @Mock
    private AdminOrderMapper adminOrderMapper;

    @Test
    void shipThrowsWhenConditionalUpdateAffectsNoRows() {
        AdminOrderShipmentSupport support = new AdminOrderShipmentSupport(adminOrderMapper,
                new OrderStateTransitionSupport());
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
}
