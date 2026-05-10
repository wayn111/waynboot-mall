package com.wayn.common.core.service.shop.support.payment;

import com.wayn.common.core.service.shop.support.common.MybatisPlusTableInfoTestHelper;

import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wayn.common.config.AlipayConfig;
import com.wayn.common.config.EpayConfig;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.mapper.shop.OrderMapper;
import com.wayn.common.core.service.shop.support.order.OrderStateTransitionSupport;
import com.wayn.util.enums.OrderStatusEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCallbackSupportTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(Order.class);
    }

    @Mock
    private WxPayService wxPayService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private PaymentPostActionSupport paymentPostActionSupport;

    @Test
    void wxPayNotifyReturnsSuccessWhenOrderAlreadyProcessed() throws Exception {
        PaymentCallbackSupport support = new PaymentCallbackSupport(new EpayConfig(), new AlipayConfig(),
                wxPayService, orderMapper, paymentPostActionSupport, new OrderStateTransitionSupport());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setContent("<xml/>".getBytes(StandardCharsets.UTF_8));

        WxPayOrderNotifyResult result = mock(WxPayOrderNotifyResult.class);
        when(wxPayService.parseOrderNotifyResult(anyString())).thenReturn(result);
        when(result.getOutTradeNo()).thenReturn("order-1");
        when(result.getTransactionId()).thenReturn("pay-1");
        when(result.getTotalFee()).thenReturn(100);

        Order order = new Order();
        order.setId(1L);
        order.setOrderSn("order-1");
        order.setOrderStatus(OrderStatusEnum.STATUS_PAY.getStatus());
        order.setActualPrice(new BigDecimal("1.00"));
        when(orderMapper.selectOne(any())).thenReturn(order);

        String response = support.wxPayNotify(request, new MockHttpServletResponse());

        assertTrue(response.contains("SUCCESS"));
        verify(orderMapper, never()).update(isNull(), any());
        verify(paymentPostActionSupport, never()).handleOrderPaid(any());
    }

    @Test
    void wxPayNotifyReturnsFailWhenAmountMismatch() throws Exception {
        PaymentCallbackSupport support = new PaymentCallbackSupport(new EpayConfig(), new AlipayConfig(),
                wxPayService, orderMapper, paymentPostActionSupport, new OrderStateTransitionSupport());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setContent("<xml/>".getBytes(StandardCharsets.UTF_8));

        WxPayOrderNotifyResult result = mock(WxPayOrderNotifyResult.class);
        when(wxPayService.parseOrderNotifyResult(anyString())).thenReturn(result);
        when(result.getOutTradeNo()).thenReturn("order-2");
        when(result.getTransactionId()).thenReturn("pay-2");
        when(result.getTotalFee()).thenReturn(100);

        Order order = new Order();
        order.setId(2L);
        order.setOrderSn("order-2");
        order.setOrderStatus(OrderStatusEnum.STATUS_CREATE.getStatus());
        order.setActualPrice(new BigDecimal("2.00"));
        when(orderMapper.selectOne(any())).thenReturn(order);

        String response = support.wxPayNotify(request, new MockHttpServletResponse());

        assertTrue(response.contains("FAIL"));
        verify(orderMapper, never()).update(isNull(), any());
        verify(paymentPostActionSupport, never()).handleOrderPaid(any());
    }

    @Test
    void wxPayNotifyReturnsSuccessWhenConditionalUpdateMissesButLatestOrderAlreadyPaid() throws Exception {
        PaymentCallbackSupport support = new PaymentCallbackSupport(new EpayConfig(), new AlipayConfig(),
                wxPayService, orderMapper, paymentPostActionSupport, new OrderStateTransitionSupport());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setContent("<xml/>".getBytes(StandardCharsets.UTF_8));

        WxPayOrderNotifyResult result = mock(WxPayOrderNotifyResult.class);
        when(wxPayService.parseOrderNotifyResult(anyString())).thenReturn(result);
        when(result.getOutTradeNo()).thenReturn("order-3");
        when(result.getTransactionId()).thenReturn("pay-3");
        when(result.getTotalFee()).thenReturn(100);

        Order order = new Order();
        order.setId(3L);
        order.setOrderSn("order-3");
        order.setOrderStatus(OrderStatusEnum.STATUS_CREATE.getStatus());
        order.setActualPrice(new BigDecimal("1.00"));
        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.update(isNull(), any())).thenReturn(0);

        Order latestOrder = new Order();
        latestOrder.setId(3L);
        latestOrder.setOrderSn("order-3");
        latestOrder.setOrderStatus(OrderStatusEnum.STATUS_PAY.getStatus());
        when(orderMapper.selectById(3L)).thenReturn(latestOrder);

        String response = support.wxPayNotify(request, new MockHttpServletResponse());

        assertTrue(response.contains("SUCCESS"));
        verify(orderMapper).update(isNull(), any());
        verify(paymentPostActionSupport, never()).handleOrderPaid(any());
    }

    @Test
    void wxPayNotifyReturnsFailWhenConditionalUpdateMissesAndOrderStillUnpaid() throws Exception {
        PaymentCallbackSupport support = new PaymentCallbackSupport(new EpayConfig(), new AlipayConfig(),
                wxPayService, orderMapper, paymentPostActionSupport, new OrderStateTransitionSupport());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setContent("<xml/>".getBytes(StandardCharsets.UTF_8));

        WxPayOrderNotifyResult result = mock(WxPayOrderNotifyResult.class);
        when(wxPayService.parseOrderNotifyResult(anyString())).thenReturn(result);
        when(result.getOutTradeNo()).thenReturn("order-4");
        when(result.getTransactionId()).thenReturn("pay-4");
        when(result.getTotalFee()).thenReturn(100);

        Order order = new Order();
        order.setId(4L);
        order.setOrderSn("order-4");
        order.setOrderStatus(OrderStatusEnum.STATUS_CREATE.getStatus());
        order.setActualPrice(new BigDecimal("1.00"));
        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.update(isNull(), any())).thenReturn(0);
        when(orderMapper.selectById(4L)).thenReturn(order);

        String response = support.wxPayNotify(request, new MockHttpServletResponse());

        assertTrue(response.contains("FAIL"));
        verify(orderMapper).update(isNull(), any());
        verify(paymentPostActionSupport, never()).handleOrderPaid(any());
    }

    @Test
    void wxPayNotifyReturnsSuccessAndRecordsPostActionWhenOrderUpdated() throws Exception {
        PaymentCallbackSupport support = new PaymentCallbackSupport(new EpayConfig(), new AlipayConfig(),
                wxPayService, orderMapper, paymentPostActionSupport, new OrderStateTransitionSupport());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setContent("<xml/>".getBytes(StandardCharsets.UTF_8));

        WxPayOrderNotifyResult result = mock(WxPayOrderNotifyResult.class);
        when(wxPayService.parseOrderNotifyResult(anyString())).thenReturn(result);
        when(result.getOutTradeNo()).thenReturn("order-5");
        when(result.getTransactionId()).thenReturn("pay-5");
        when(result.getTotalFee()).thenReturn(100);

        Order order = new Order();
        order.setId(5L);
        order.setOrderSn("order-5");
        order.setOrderStatus(OrderStatusEnum.STATUS_CREATE.getStatus());
        order.setActualPrice(new BigDecimal("1.00"));
        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.update(isNull(), any())).thenReturn(1);

        String response = support.wxPayNotify(request, new MockHttpServletResponse());

        assertTrue(response.contains("SUCCESS"));
        verify(orderMapper).update(isNull(), any());
        verify(paymentPostActionSupport).handleOrderPaid(5L);
    }
}
