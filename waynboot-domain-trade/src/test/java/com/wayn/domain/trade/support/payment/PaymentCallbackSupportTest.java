package com.wayn.domain.trade.support.payment;

import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wayn.common.config.AlipayConfig;
import com.wayn.common.config.EpayConfig;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.enums.PaymentFlowSaveResult;
import com.wayn.domain.api.trade.mapper.OrderMapper;
import com.wayn.domain.api.trade.service.OrderStatusLogService;
import com.wayn.domain.api.trade.service.PaymentFlowService;
import com.wayn.domain.api.common.MybatisPlusTableInfoTestHelper;
import com.wayn.domain.trade.support.order.OrderStateTransitionSupport;
import com.wayn.util.enums.OrderStatusEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
    @Mock
    private PaymentFlowService paymentFlowService;
    @Mock
    private OrderStatusLogService orderStatusLogService;
    private PaymentCallbackSupport support;

    /**
     * 初始化支付回调支撑服务。
     * 单测不注入事务模板，便于直接验证支付状态更新、流水保存和后置消息的分支行为。
     */
    @BeforeEach
    void setUp() {
        support = new PaymentCallbackSupport(new EpayConfig(), new AlipayConfig(), wxPayService, orderMapper,
                paymentPostActionSupport, new OrderStateTransitionSupport(), paymentFlowService, orderStatusLogService);
    }

    /**
     * 已支付订单收到重复微信回调时应直接返回成功。
     * 该分支不能再次更新订单，也不能重复写支付后置动作。
     */
    @Test
    void wxPayNotifyReturnsSuccessWhenOrderAlreadyProcessed() throws Exception {
        MockHttpServletRequest request = wxNotifyRequest();
        mockWxNotify("order-1", "pay-1", 100);

        Order order = newOrder(1L, "order-1", OrderStatusEnum.STATUS_PAY, "1.00");
        when(orderMapper.selectOne(any())).thenReturn(order);

        String response = support.wxPayNotify(request, new MockHttpServletResponse());

        assertTrue(response.contains("SUCCESS"));
        verify(orderMapper, never()).update(isNull(), any());
        verify(paymentPostActionSupport, never()).handleOrderPaid(any());
    }

    /**
     * 微信回调金额和订单实付金额不一致时应返回失败。
     * 金额校验失败必须发生在订单状态更新之前。
     */
    @Test
    void wxPayNotifyReturnsFailWhenAmountMismatch() throws Exception {
        MockHttpServletRequest request = wxNotifyRequest();
        mockWxNotify("order-2", "pay-2", 100);

        Order order = newOrder(2L, "order-2", OrderStatusEnum.STATUS_CREATE, "2.00");
        when(orderMapper.selectOne(any())).thenReturn(order);

        String response = support.wxPayNotify(request, new MockHttpServletResponse());

        assertTrue(response.contains("FAIL"));
        verify(orderMapper, never()).update(isNull(), any());
        verify(paymentPostActionSupport, never()).handleOrderPaid(any());
    }

    /**
     * 条件更新未命中但最新订单已支付时应按幂等成功处理。
     * 该场景模拟并发支付回调中另一个请求已先完成状态推进。
     */
    @Test
    void wxPayNotifyReturnsSuccessWhenConditionalUpdateMissesButLatestOrderAlreadyPaid() throws Exception {
        MockHttpServletRequest request = wxNotifyRequest();
        mockWxNotify("order-3", "pay-3", 100);

        Order order = newOrder(3L, "order-3", OrderStatusEnum.STATUS_CREATE, "1.00");
        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.update(isNull(), any())).thenReturn(0);

        Order latestOrder = newOrder(3L, "order-3", OrderStatusEnum.STATUS_PAY, "1.00");
        when(orderMapper.selectById(3L)).thenReturn(latestOrder);

        String response = support.wxPayNotify(request, new MockHttpServletResponse());

        assertTrue(response.contains("SUCCESS"));
        verify(orderMapper).update(isNull(), any());
        verify(paymentPostActionSupport, never()).handleOrderPaid(any());
    }

    /**
     * 条件更新未命中且订单仍未支付时应返回失败。
     * 该分支表示状态推进没有成功，不能写后置动作消息。
     */
    @Test
    void wxPayNotifyReturnsFailWhenConditionalUpdateMissesAndOrderStillUnpaid() throws Exception {
        MockHttpServletRequest request = wxNotifyRequest();
        mockWxNotify("order-4", "pay-4", 100);

        Order order = newOrder(4L, "order-4", OrderStatusEnum.STATUS_CREATE, "1.00");
        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.update(isNull(), any())).thenReturn(0);
        when(orderMapper.selectById(4L)).thenReturn(order);

        String response = support.wxPayNotify(request, new MockHttpServletResponse());

        assertTrue(response.contains("FAIL"));
        verify(orderMapper).update(isNull(), any());
        verify(paymentPostActionSupport, never()).handleOrderPaid(any());
    }

    /**
     * 待支付订单被微信回调成功推进后，应保存支付流水、记录状态日志并写后置动作消息。
     * 这是支付回调主成功路径。
     */
    @Test
    void wxPayNotifyReturnsSuccessAndRecordsPostActionWhenOrderUpdated() throws Exception {
        MockHttpServletRequest request = wxNotifyRequest();
        mockWxNotify("order-5", "pay-5", 100);

        Order order = newOrder(5L, "order-5", OrderStatusEnum.STATUS_CREATE, "1.00");
        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.update(isNull(), any())).thenReturn(1);
        when(paymentFlowService.savePaidFlow(any())).thenReturn(PaymentFlowSaveResult.CREATED);

        String response = support.wxPayNotify(request, new MockHttpServletResponse());

        assertTrue(response.contains("SUCCESS"));
        verify(orderMapper).update(isNull(), any());
        verify(paymentFlowService).savePaidFlow(any());
        verify(orderStatusLogService).recordSuccess(any());
        verify(paymentPostActionSupport).handleOrderPaid(5L);
    }

    /**
     * 订单已更新但支付流水发生唯一键冲突时应返回失败。
     * 生产环境带事务模板时该异常会回滚订单状态，防止同一渠道流水绑定多个订单。
     */
    @Test
    void wxPayNotifyReturnsFailWhenPaymentFlowConflictsAfterOrderUpdated() throws Exception {
        MockHttpServletRequest request = wxNotifyRequest();
        mockWxNotify("order-6", "pay-duplicate", 100);

        Order order = newOrder(6L, "order-6", OrderStatusEnum.STATUS_CREATE, "1.00");
        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.update(isNull(), any())).thenReturn(1);
        when(paymentFlowService.savePaidFlow(any())).thenReturn(PaymentFlowSaveResult.DUPLICATE_CONFLICT);

        String response = support.wxPayNotify(request, new MockHttpServletResponse());

        assertTrue(response.contains("FAIL"));
        verify(paymentFlowService).savePaidFlow(any());
        verify(paymentPostActionSupport, never()).handleOrderPaid(any());
    }

    /**
     * 构建微信回调 HTTP 请求。
     *
     * @return 微信回调请求
     */
    private MockHttpServletRequest wxNotifyRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setContent("<xml/>".getBytes(StandardCharsets.UTF_8));
        return request;
    }

    /**
     * 模拟微信支付 SDK 解析出的回调结果。
     *
     * @param orderSn 订单号
     * @param payId 第三方支付流水号
     * @param totalFee 支付金额，单位为分
     * @throws Exception 微信 SDK 解析异常
     */
    private void mockWxNotify(String orderSn, String payId, int totalFee) throws Exception {
        WxPayOrderNotifyResult result = mock(WxPayOrderNotifyResult.class);
        when(wxPayService.parseOrderNotifyResult(anyString())).thenReturn(result);
        when(result.getOutTradeNo()).thenReturn(orderSn);
        when(result.getTransactionId()).thenReturn(payId);
        when(result.getTotalFee()).thenReturn(totalFee);
    }

    /**
     * 构建订单测试数据。
     *
     * @param orderId 订单 ID
     * @param orderSn 订单号
     * @param status 订单状态
     * @param actualPrice 实付金额
     * @return 订单实体
     */
    private Order newOrder(Long orderId, String orderSn, OrderStatusEnum status, String actualPrice) {
        Order order = new Order();
        order.setId(orderId);
        order.setOrderSn(orderSn);
        order.setOrderStatus(status.getStatus());
        order.setActualPrice(new BigDecimal(actualPrice));
        return order;
    }
}
