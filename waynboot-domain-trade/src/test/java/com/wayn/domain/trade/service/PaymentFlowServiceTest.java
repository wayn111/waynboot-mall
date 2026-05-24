package com.wayn.domain.trade.service;

import com.wayn.domain.api.trade.entity.PaymentFlow;
import com.wayn.domain.api.trade.enums.PaymentFlowSaveResult;
import com.wayn.domain.api.trade.mapper.PaymentFlowMapper;
import com.wayn.domain.api.trade.service.PaymentFlowCreateCommand;
import com.wayn.domain.api.trade.service.PaymentFlowService;
import com.wayn.domain.api.common.MybatisPlusTableInfoTestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentFlowServiceTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(PaymentFlow.class);
    }

    /**
     * 验证支付流水首次写入成功时返回 CREATED。
     */
    @Test
    void savePaidFlowReturnsCreatedWhenInsertSucceeds() {
        PaymentFlowMapper mapper = mock(PaymentFlowMapper.class);
        PaymentFlowService service = new PaymentFlowService(mapper);
        when(mapper.insert(any(PaymentFlow.class))).thenReturn(1);

        PaymentFlowSaveResult result = service.savePaidFlow(buildCommand("WECHAT:pay-1", "order-1"));

        assertEquals(PaymentFlowSaveResult.CREATED, result);
        verify(mapper).insert(any(PaymentFlow.class));
    }

    /**
     * 验证同一渠道流水重复通知同一订单时返回 DUPLICATE_SAME_ORDER。
     */
    @Test
    void savePaidFlowReturnsDuplicateSameOrderWhenFlowAlreadyExistsForSameOrder() {
        PaymentFlowMapper mapper = mock(PaymentFlowMapper.class);
        PaymentFlowService service = new PaymentFlowService(mapper);
        when(mapper.insert(any(PaymentFlow.class))).thenThrow(new DuplicateKeyException("duplicate"));
        PaymentFlow existing = new PaymentFlow();
        existing.setFlowKey("WECHAT:pay-1");
        existing.setOrderSn("order-1");
        when(mapper.selectOne(any())).thenReturn(existing);

        PaymentFlowSaveResult result = service.savePaidFlow(buildCommand("WECHAT:pay-1", "order-1"));

        assertEquals(PaymentFlowSaveResult.DUPLICATE_SAME_ORDER, result);
    }

    /**
     * 验证同一渠道流水重复绑定不同订单时返回 DUPLICATE_CONFLICT。
     */
    @Test
    void savePaidFlowReturnsConflictWhenFlowAlreadyExistsForDifferentOrder() {
        PaymentFlowMapper mapper = mock(PaymentFlowMapper.class);
        PaymentFlowService service = new PaymentFlowService(mapper);
        when(mapper.insert(any(PaymentFlow.class))).thenThrow(new DuplicateKeyException("duplicate"));
        PaymentFlow existing = new PaymentFlow();
        existing.setFlowKey("WECHAT:pay-1");
        existing.setOrderSn("order-other");
        when(mapper.selectOne(any())).thenReturn(existing);

        PaymentFlowSaveResult result = service.savePaidFlow(buildCommand("WECHAT:pay-1", "order-1"));

        assertEquals(PaymentFlowSaveResult.DUPLICATE_CONFLICT, result);
    }

    /**
     * 验证唯一键冲突后未查到原流水时按冲突处理，避免异常数据被误判为幂等成功。
     */
    @Test
    void savePaidFlowReturnsConflictWhenDuplicateFlowCannotBeLoaded() {
        PaymentFlowMapper mapper = mock(PaymentFlowMapper.class);
        PaymentFlowService service = new PaymentFlowService(mapper);
        when(mapper.insert(any(PaymentFlow.class))).thenThrow(new DuplicateKeyException("duplicate"));
        when(mapper.selectOne(any())).thenReturn(null);

        PaymentFlowSaveResult result = service.savePaidFlow(buildCommand("WECHAT:pay-1", "order-1"));

        assertEquals(PaymentFlowSaveResult.DUPLICATE_CONFLICT, result);
    }

    /**
     * 空支付流水命令应直接按冲突处理。
     * 支付回调缺少命令上下文时不能落库，也不能误判为幂等成功。
     */
    @Test
    void savePaidFlowReturnsConflictWhenCommandIsNull() {
        PaymentFlowMapper mapper = mock(PaymentFlowMapper.class);
        PaymentFlowService service = new PaymentFlowService(mapper);

        PaymentFlowSaveResult result = service.savePaidFlow(null);

        assertEquals(PaymentFlowSaveResult.DUPLICATE_CONFLICT, result);
        verify(mapper, never()).insert(any(PaymentFlow.class));
    }

    /**
     * flowKey 缺失时应直接按冲突处理。
     * flowKey 是支付流水唯一键，缺失后无法保证重复回调幂等。
     */
    @Test
    void savePaidFlowReturnsConflictWhenFlowKeyIsBlank() {
        PaymentFlowMapper mapper = mock(PaymentFlowMapper.class);
        PaymentFlowService service = new PaymentFlowService(mapper);

        PaymentFlowSaveResult result = service.savePaidFlow(buildCommand(" ", "order-1"));

        assertEquals(PaymentFlowSaveResult.DUPLICATE_CONFLICT, result);
        verify(mapper, never()).insert(any(PaymentFlow.class));
    }

    /**
     * 第三方支付流水号缺失时应直接按冲突处理。
     * payId 是渠道侧唯一交易身份的一部分，缺失后不能支撑支付对账和重复回调定位。
     */
    @Test
    void savePaidFlowReturnsConflictWhenPayIdIsBlank() {
        PaymentFlowMapper mapper = mock(PaymentFlowMapper.class);
        PaymentFlowService service = new PaymentFlowService(mapper);

        PaymentFlowSaveResult result = service.savePaidFlow(buildCommand("WECHAT:pay-1", "order-1", " ", "WECHAT",
                new BigDecimal("1.00")));

        assertEquals(PaymentFlowSaveResult.DUPLICATE_CONFLICT, result);
        verify(mapper, never()).insert(any(PaymentFlow.class));
    }

    /**
     * 支付渠道缺失时应直接按冲突处理。
     * payChannel 缺失会导致 flowKey 和渠道账单身份无法被可靠追溯。
     */
    @Test
    void savePaidFlowReturnsConflictWhenPayChannelIsBlank() {
        PaymentFlowMapper mapper = mock(PaymentFlowMapper.class);
        PaymentFlowService service = new PaymentFlowService(mapper);

        PaymentFlowSaveResult result = service.savePaidFlow(buildCommand("WECHAT:pay-1", "order-1", "pay-1", " ",
                new BigDecimal("1.00")));

        assertEquals(PaymentFlowSaveResult.DUPLICATE_CONFLICT, result);
        verify(mapper, never()).insert(any(PaymentFlow.class));
    }

    /**
     * 支付金额为空或非正数时应直接按冲突处理。
     * 支付流水是后续对账依据，非法金额进入库内会放大日终对账差异。
     */
    @Test
    void savePaidFlowReturnsConflictWhenPayAmountIsInvalid() {
        PaymentFlowMapper mapper = mock(PaymentFlowMapper.class);
        PaymentFlowService service = new PaymentFlowService(mapper);

        PaymentFlowSaveResult nullAmountResult = service.savePaidFlow(buildCommand("WECHAT:pay-1", "order-1",
                "pay-1", "WECHAT", null));
        PaymentFlowSaveResult zeroAmountResult = service.savePaidFlow(buildCommand("WECHAT:pay-2", "order-2",
                "pay-2", "WECHAT", BigDecimal.ZERO));

        assertEquals(PaymentFlowSaveResult.DUPLICATE_CONFLICT, nullAmountResult);
        assertEquals(PaymentFlowSaveResult.DUPLICATE_CONFLICT, zeroAmountResult);
        verify(mapper, never()).insert(any(PaymentFlow.class));
    }

    /**
     * 构建支付流水创建命令。
     *
     * @param flowKey 支付流水幂等键
     * @param orderSn 订单号
     * @return 支付流水创建命令
     */
    private PaymentFlowCreateCommand buildCommand(String flowKey, String orderSn) {
        return buildCommand(flowKey, orderSn, "pay-1", "WECHAT", new BigDecimal("1.00"));
    }

    /**
     * 构建支付流水创建命令。
     *
     * @param flowKey 支付流水幂等键
     * @param orderSn 订单号
     * @param payId 第三方支付流水号
     * @param payChannel 支付渠道编码
     * @param payAmount 支付金额
     * @return 支付流水创建命令
     */
    private PaymentFlowCreateCommand buildCommand(String flowKey, String orderSn, String payId, String payChannel,
                                                  BigDecimal payAmount) {
        return PaymentFlowCreateCommand.builder()
                .flowKey(flowKey)
                .orderId(1L)
                .orderSn(orderSn)
                .payId(payId)
                .payChannel(payChannel)
                .payAmount(payAmount)
                .build();
    }
}
