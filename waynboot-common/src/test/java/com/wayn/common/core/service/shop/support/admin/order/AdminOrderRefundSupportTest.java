package com.wayn.common.core.service.shop.support.admin.order;

import com.alipay.api.AlipayApiException;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.mapper.shop.AdminOrderMapper;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.core.service.shop.OrderStatusLogService;
import com.wayn.common.core.service.shop.support.common.MybatisPlusTableInfoTestHelper;
import com.wayn.common.core.service.shop.support.order.OrderStateTransitionSupport;
import com.wayn.common.core.service.shop.support.order.OrderStockSupport;
import com.wayn.common.design.strategy.refund.context.RefundContext;
import com.wayn.common.design.strategy.refund.strategy.RefundInterface;
import com.wayn.common.model.request.OrderRefundReqVO;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.RefundStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderRefundSupportTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(Order.class);
    }

    @Mock
    private AdminOrderMapper adminOrderMapper;
    @Mock
    private IOrderGoodsService orderGoodsService;
    @Mock
    private OrderStockSupport orderStockSupport;
    @Mock
    private RefundContext refundContext;
    @Mock
    private PlatformTransactionManager platformTransactionManager;
    @Mock
    private OrderStatusLogService orderStatusLogService;

    @Test
    void refundMarksOrderAsFailedWhenThirdPartyRefundThrows() throws UnsupportedEncodingException, WxPayException, AlipayApiException {
        AdminOrderRefundSupport support = buildSupport();
        OrderRefundReqVO reqVO = buildRefundRequest();
        Order order = buildRefundOrder();
        RefundInterface refundInterface = mock(RefundInterface.class);
        TransactionStatus transactionStatus = mock(TransactionStatus.class);

        when(adminOrderMapper.selectOne(any())).thenReturn(order);
        when(orderGoodsService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<OrderGoods>>any()))
                .thenReturn(List.of(buildOrderGoods()));
        when(refundContext.getInstance(order.getPayType())).thenReturn(refundInterface);
        when(platformTransactionManager.getTransaction(any())).thenReturn(transactionStatus);
        when(adminOrderMapper.update(any(Order.class), any())).thenReturn(1);
        org.mockito.Mockito.doThrow(new RuntimeException("refund error")).when(refundInterface).refund(reqVO);

        support.refund(reqVO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(adminOrderMapper).update(orderCaptor.capture(), any());
        Order updated = orderCaptor.getValue();
        assertEquals(OrderStatusEnum.STATUS_REFUND.getStatus(), updated.getOrderStatus());
        assertEquals(RefundStatusEnum.REFUND_FAIL.getStatus(), updated.getRefundStatus());
        assertEquals(BigDecimal.ZERO, updated.getRefundAmount());
        assertEquals(order.getPayType(), updated.getRefundType());
        verify(orderStockSupport, never()).restoreStock(any());
        verify(platformTransactionManager).commit(transactionStatus);
    }

    @Test
    void refundThrowsWhenConditionalUpdateAffectsNoRows() throws UnsupportedEncodingException, WxPayException, AlipayApiException {
        AdminOrderRefundSupport support = buildSupport();
        OrderRefundReqVO reqVO = buildRefundRequest();
        Order order = buildRefundOrder();
        RefundInterface refundInterface = mock(RefundInterface.class);
        TransactionStatus transactionStatus = mock(TransactionStatus.class);

        when(adminOrderMapper.selectOne(any())).thenReturn(order);
        when(orderGoodsService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<OrderGoods>>any()))
                .thenReturn(List.of(buildOrderGoods()));
        when(refundContext.getInstance(order.getPayType())).thenReturn(refundInterface);
        when(platformTransactionManager.getTransaction(any())).thenReturn(transactionStatus);
        when(adminOrderMapper.update(any(Order.class), any())).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> support.refund(reqVO));

        assertEquals(ReturnCodeEnum.ORDER_CANNOT_REFUND_ERROR.getCode(), exception.getCode());
        verify(platformTransactionManager).rollback(transactionStatus);
        verify(orderStockSupport, never()).restoreStock(any());
    }

    @Test
    void refundRestoresStockWhenThirdPartyRefundSucceeds() throws UnsupportedEncodingException, WxPayException, AlipayApiException {
        AdminOrderRefundSupport support = buildSupport();
        OrderRefundReqVO reqVO = buildRefundRequest();
        Order order = buildRefundOrder();
        RefundInterface refundInterface = mock(RefundInterface.class);
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        List<OrderGoods> orderGoodsList = List.of(buildOrderGoods());

        when(adminOrderMapper.selectOne(any())).thenReturn(order);
        when(orderGoodsService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<OrderGoods>>any()))
                .thenReturn(orderGoodsList);
        when(refundContext.getInstance(order.getPayType())).thenReturn(refundInterface);
        when(platformTransactionManager.getTransaction(any())).thenReturn(transactionStatus);
        when(adminOrderMapper.update(any(Order.class), any())).thenReturn(1);

        support.refund(reqVO);

        verify(orderStockSupport).restoreStock(orderGoodsList);
        verify(orderStatusLogService).recordSuccess(any());
        verify(platformTransactionManager).commit(transactionStatus);
    }

    private AdminOrderRefundSupport buildSupport() {
        return new AdminOrderRefundSupport(adminOrderMapper, orderGoodsService, orderStockSupport,
                refundContext, platformTransactionManager, new OrderStateTransitionSupport(), orderStatusLogService);
    }

    private OrderRefundReqVO buildRefundRequest() {
        OrderRefundReqVO reqVO = new OrderRefundReqVO();
        reqVO.setOrderSn("order-1");
        reqVO.setRefundMoney(new BigDecimal("20.00"));
        reqVO.setRefundReason("用户申请");
        return reqVO;
    }

    private Order buildRefundOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderSn("order-1");
        order.setPayId("pay-1");
        order.setPayType(1);
        order.setActualPrice(new BigDecimal("50.00"));
        order.setOrderStatus(OrderStatusEnum.STATUS_REFUND.getStatus());
        return order;
    }

    private OrderGoods buildOrderGoods() {
        OrderGoods orderGoods = new OrderGoods();
        orderGoods.setProductId(9L);
        orderGoods.setNumber(2);
        return orderGoods;
    }
}
