package com.wayn.domain.trade.service;

import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.entity.PaymentChannelBill;
import com.wayn.domain.api.trade.entity.PaymentFlow;
import com.wayn.domain.api.trade.entity.PaymentRefundFlow;
import com.wayn.domain.api.trade.mapper.OrderMapper;
import com.wayn.domain.api.trade.mapper.PaymentChannelBillMapper;
import com.wayn.domain.api.trade.mapper.PaymentFlowMapper;
import com.wayn.domain.api.trade.mapper.PaymentRefundFlowMapper;
import com.wayn.domain.api.trade.service.PaymentReconciliationDifference;
import com.wayn.domain.api.trade.service.PaymentReconciliationQuery;
import com.wayn.domain.api.trade.service.PaymentReconciliationService;
import com.wayn.domain.trade.service.impl.PaymentReconciliationServiceImpl;
import com.wayn.domain.api.trade.service.PaymentReconciliationSummary;
import com.wayn.domain.api.common.MybatisPlusTableInfoTestHelper;
import com.wayn.util.enums.OrderStatusEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentReconciliationServiceTest {

    @Mock
    private PaymentFlowMapper paymentFlowMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private PaymentChannelBillMapper channelBillMapper;

    @Mock
    private PaymentRefundFlowMapper refundFlowMapper;

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(PaymentFlow.class);
        MybatisPlusTableInfoTestHelper.init(PaymentChannelBill.class);
        MybatisPlusTableInfoTestHelper.init(PaymentRefundFlow.class);
        MybatisPlusTableInfoTestHelper.init(Order.class);
    }

    @Test
    void reconcileFindsAmountMismatchAndMissingPaymentFlow() {
        PaymentFlow flow = new PaymentFlow();
        flow.setOrderSn("order-1");
        flow.setPayAmount(new BigDecimal("10.00"));
        Order paidOrder = buildOrder(1L, "order-1", new BigDecimal("12.00"), OrderStatusEnum.STATUS_PAY);
        Order missingFlowOrder = buildOrder(2L, "order-2", new BigDecimal("8.00"), OrderStatusEnum.STATUS_PAY);

        when(paymentFlowMapper.selectList(any())).thenReturn(List.of(flow));
        when(orderMapper.selectList(any())).thenReturn(List.of(paidOrder, missingFlowOrder));
        when(paymentFlowMapper.selectOne(any())).thenReturn(null);
        when(channelBillMapper.selectList(any())).thenReturn(List.of());
        when(refundFlowMapper.selectList(any())).thenReturn(List.of());

        PaymentReconciliationSummary summary = newService().reconcile(PaymentReconciliationQuery.defaultQuery());

        assertEquals(2, summary.getDifferenceCount());
        assertEquals(1, summary.getAmountMismatchCount());
        assertEquals(1, summary.getMissingPaymentFlowCount());
        assertTrue(summary.getDifferences().stream()
                .anyMatch(difference -> "AMOUNT_MISMATCH".equals(difference.getDifferenceType())));
        assertTrue(summary.getDifferences().stream()
                .anyMatch(difference -> "ORDER_WITHOUT_FLOW".equals(difference.getDifferenceType())));
    }

    @Test
    void reconcileIncludesChannelBillAndRefundFlowDifferences() {
        PaymentChannelBill channelBill = new PaymentChannelBill();
        channelBill.setOrderSn("order-channel");
        channelBill.setPayId("pay-1");
        channelBill.setPayChannel("WX");
        channelBill.setPayAmount(new BigDecimal("10.00"));
        PaymentRefundFlow refundFlow = new PaymentRefundFlow();
        refundFlow.setOrderSn("order-refund");
        refundFlow.setRefundAmount(new BigDecimal("3.00"));

        when(paymentFlowMapper.selectList(any())).thenReturn(List.of());
        when(orderMapper.selectList(any())).thenReturn(List.of());
        when(channelBillMapper.selectList(any())).thenReturn(List.of(channelBill));
        when(refundFlowMapper.selectList(any())).thenReturn(List.of(refundFlow));

        PaymentReconciliationSummary summary = newService().reconcile(PaymentReconciliationQuery.defaultQuery());

        assertEquals(2, summary.getDifferenceCount());
        assertEquals(1, summary.getChannelBillCount());
        assertEquals(1, summary.getRefundFlowCount());
        assertTrue(summary.getDifferences().stream()
                .anyMatch(difference -> "CHANNEL_BILL_WITHOUT_FLOW".equals(difference.getDifferenceType())));
        assertTrue(summary.getDifferences().stream()
                .anyMatch(difference -> "REFUND_FLOW_WITHOUT_ORDER".equals(difference.getDifferenceType())));
    }

    /**
     * 支付对账扫描不能按支付流水逐条反查订单。
     * 大批量日终对账时 N+1 查询会放大数据库压力，服务应按订单号集合批量加载订单快照。
     */
    @Test
    void reconcileLoadsOrdersInBatchForPaymentFlows() {
        PaymentFlow firstFlow = buildPaymentFlow("order-1", new BigDecimal("10.00"));
        PaymentFlow secondFlow = buildPaymentFlow("order-2", new BigDecimal("20.00"));
        Order firstOrder = buildOrder(1L, "order-1", new BigDecimal("10.00"), OrderStatusEnum.STATUS_PAY);
        Order secondOrder = buildOrder(2L, "order-2", new BigDecimal("20.00"), OrderStatusEnum.STATUS_PAY);

        when(paymentFlowMapper.selectList(any())).thenReturn(List.of(firstFlow, secondFlow));
        when(orderMapper.selectList(any()))
                .thenReturn(List.of(firstOrder, secondOrder))
                .thenReturn(List.of(firstOrder, secondOrder));
        when(channelBillMapper.selectList(any())).thenReturn(List.of());
        when(refundFlowMapper.selectList(any())).thenReturn(List.of());

        PaymentReconciliationSummary summary = newService().reconcile(PaymentReconciliationQuery.defaultQuery());

        assertEquals(0, summary.getDifferenceCount());
        verify(orderMapper, times(2)).selectList(any());
    }

    /**
     * 渠道账单对账不能按账单逐条反查内部支付流水。
     * 渠道账单文件通常按天导入，批量反查可以降低日终对账期间的数据库往返次数。
     */
    @Test
    void reconcileLoadsPaymentFlowsInBatchForChannelBills() {
        PaymentChannelBill firstBill = buildChannelBill("order-1", "pay-1", "WX", new BigDecimal("10.00"));
        PaymentChannelBill secondBill = buildChannelBill("order-2", "pay-2", "ALI", new BigDecimal("20.00"));
        PaymentFlow firstFlow = buildPaymentFlow("order-1", new BigDecimal("10.00"));
        firstFlow.setPayId("pay-1");
        firstFlow.setPayChannel("WX");
        PaymentFlow secondFlow = buildPaymentFlow("order-2", new BigDecimal("20.00"));
        secondFlow.setPayId("pay-2");
        secondFlow.setPayChannel("ALI");

        when(paymentFlowMapper.selectList(any()))
                .thenReturn(List.of())
                .thenReturn(List.of(firstFlow, secondFlow));
        when(orderMapper.selectList(any())).thenReturn(List.of());
        when(channelBillMapper.selectList(any())).thenReturn(List.of(firstBill, secondBill));
        when(refundFlowMapper.selectList(any())).thenReturn(List.of());

        PaymentReconciliationSummary summary = newService().reconcile(PaymentReconciliationQuery.defaultQuery());

        assertEquals(0, summary.getDifferenceCount());
        verify(paymentFlowMapper, times(2)).selectList(any());
    }

    /**
     * 渠道账单缺少完整支付身份时不能再批量反查内部流水。
     * 空流水号本身已经足以形成渠道账单差异，继续查询 PaymentFlow 只会在日终对账中制造无效 DB 压力。
     */
    @Test
    void reconcileSkipsPaymentFlowLookupWhenChannelBillIdentityIsBlank() {
        PaymentChannelBill channelBill = buildChannelBill("order-blank", " ", "WX", new BigDecimal("10.00"));

        when(paymentFlowMapper.selectList(any())).thenReturn(List.of());
        when(orderMapper.selectList(any())).thenReturn(List.of());
        when(channelBillMapper.selectList(any())).thenReturn(List.of(channelBill));
        when(refundFlowMapper.selectList(any())).thenReturn(List.of());

        PaymentReconciliationSummary summary = newService().reconcile(PaymentReconciliationQuery.defaultQuery());

        assertEquals(1, summary.getDifferenceCount());
        assertEquals(1, summary.getChannelBillMismatchCount());
        assertTrue(summary.getDifferences().stream()
                .anyMatch(difference -> "CHANNEL_BILL_WITHOUT_FLOW".equals(difference.getDifferenceType())));
        verify(paymentFlowMapper, times(1)).selectList(any());
    }

    /**
     * 退款流水对账不能按流水逐条反查订单。
     * 退款补偿和日终对账会同时放大退款记录扫描量，批量加载订单可以避免 N+1 查询。
     */
    @Test
    void reconcileLoadsOrdersInBatchForRefundFlows() {
        PaymentRefundFlow firstRefundFlow = buildRefundFlow("order-1", new BigDecimal("3.00"));
        PaymentRefundFlow secondRefundFlow = buildRefundFlow("order-2", new BigDecimal("5.00"));
        Order firstOrder = buildOrder(1L, "order-1", new BigDecimal("10.00"), OrderStatusEnum.STATUS_REFUND);
        firstOrder.setRefundAmount(new BigDecimal("3.00"));
        Order secondOrder = buildOrder(2L, "order-2", new BigDecimal("20.00"), OrderStatusEnum.STATUS_REFUND);
        secondOrder.setRefundAmount(new BigDecimal("5.00"));

        when(paymentFlowMapper.selectList(any())).thenReturn(List.of());
        when(orderMapper.selectList(any()))
                .thenReturn(List.of())
                .thenReturn(List.of(firstOrder, secondOrder));
        when(channelBillMapper.selectList(any())).thenReturn(List.of());
        when(refundFlowMapper.selectList(any())).thenReturn(List.of(firstRefundFlow, secondRefundFlow));

        PaymentReconciliationSummary summary = newService().reconcile(PaymentReconciliationQuery.defaultQuery());

        assertEquals(0, summary.getDifferenceCount());
        verify(orderMapper, times(2)).selectList(any());
    }

    /**
     * 对账扫描应忽略 Mapper 返回列表中的空元素。
     * 日终对账更关注差异沉淀，单条脏对象不能中断整批支付、账单或退款扫描。
     */
    @Test
    void reconcileIgnoresNullRowsReturnedByMappers() {
        when(paymentFlowMapper.selectList(any())).thenReturn(listWithNullElement());
        when(orderMapper.selectList(any())).thenReturn(listWithNullElement());
        when(channelBillMapper.selectList(any())).thenReturn(listWithNullElement());
        when(refundFlowMapper.selectList(any())).thenReturn(listWithNullElement());

        PaymentReconciliationSummary summary = assertDoesNotThrow(
                () -> newService().reconcile(PaymentReconciliationQuery.defaultQuery()));

        assertEquals(0, summary.getDifferenceCount());
        assertEquals(0, summary.getPaymentFlowCount());
        assertEquals(0, summary.getPaidOrderCount());
        assertEquals(0, summary.getChannelBillCount());
        assertEquals(0, summary.getRefundFlowCount());
    }

    /**
     * 创建支付对账服务。
     * 每个用例复用同一组 Mock 字段，避免重复构造 Mapper 时遗漏新增依赖。
     *
     * @return 支付对账服务
     */
    private PaymentReconciliationService newService() {
        return new PaymentReconciliationServiceImpl(paymentFlowMapper, orderMapper, channelBillMapper, refundFlowMapper);
    }

    /**
     * 构建支付流水测试对象。
     *
     * @param orderSn 订单号
     * @param payAmount 支付金额
     * @return 支付流水对象
     */
    private PaymentFlow buildPaymentFlow(String orderSn, BigDecimal payAmount) {
        PaymentFlow paymentFlow = new PaymentFlow();
        paymentFlow.setOrderSn(orderSn);
        paymentFlow.setPayAmount(payAmount);
        paymentFlow.setPayId("pay-" + orderSn);
        paymentFlow.setPayChannel("WX");
        return paymentFlow;
    }

    /**
     * 构建渠道账单测试对象。
     *
     * @param orderSn 订单号
     * @param payId 第三方支付流水号
     * @param payChannel 支付渠道
     * @param payAmount 渠道账单金额
     * @return 渠道账单对象
     */
    private PaymentChannelBill buildChannelBill(String orderSn, String payId, String payChannel,
                                                BigDecimal payAmount) {
        PaymentChannelBill channelBill = new PaymentChannelBill();
        channelBill.setOrderSn(orderSn);
        channelBill.setPayId(payId);
        channelBill.setPayChannel(payChannel);
        channelBill.setPayAmount(payAmount);
        return channelBill;
    }

    /**
     * 构建退款流水测试对象。
     *
     * @param orderSn 订单号
     * @param refundAmount 退款金额
     * @return 退款流水对象
     */
    private PaymentRefundFlow buildRefundFlow(String orderSn, BigDecimal refundAmount) {
        PaymentRefundFlow refundFlow = new PaymentRefundFlow();
        refundFlow.setOrderSn(orderSn);
        refundFlow.setRefundAmount(refundAmount);
        return refundFlow;
    }

    /**
     * 构建订单测试对象。
     *
     * @param id 订单 ID
     * @param orderSn 订单号
     * @param actualPrice 实付金额
     * @param statusEnum 订单状态
     * @return 订单对象
     */
    private Order buildOrder(Long id, String orderSn, BigDecimal actualPrice, OrderStatusEnum statusEnum) {
        Order order = new Order();
        order.setId(id);
        order.setOrderSn(orderSn);
        order.setActualPrice(actualPrice);
        order.setOrderStatus(statusEnum.getStatus());
        return order;
    }

    /**
     * 构造只包含空元素的 Mapper 返回值。
     *
     * @param <T> 元素类型
     * @return 只包含空元素的列表
     */
    private <T> List<T> listWithNullElement() {
        return java.util.Collections.singletonList(null);
    }
}


