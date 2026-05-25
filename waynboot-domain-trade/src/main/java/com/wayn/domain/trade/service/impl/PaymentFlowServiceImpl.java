package com.wayn.domain.trade.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.domain.api.trade.entity.PaymentFlow;
import com.wayn.domain.api.trade.enums.PaymentFlowSaveResult;
import com.wayn.domain.api.trade.enums.PaymentFlowStatusEnum;
import com.wayn.domain.api.trade.mapper.PaymentFlowMapper;
import com.wayn.domain.api.trade.service.PaymentFlowCreateCommand;
import com.wayn.domain.api.trade.service.PaymentFlowService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * 支付流水服务实现。
 * 负责保存渠道支付成功流水，并把数据库唯一键冲突转换成同单幂等或跨单冲突结果。
 */
@Slf4j
@Service
@AllArgsConstructor
public class PaymentFlowServiceImpl implements PaymentFlowService {

    private final PaymentFlowMapper paymentFlowMapper;

    /**
     * 保存支付成功流水。
     * 该方法应在支付回调事务内调用；同一 flowKey 重复通知同一订单返回幂等结果，绑定不同订单返回冲突结果。
     *
     * @param command 支付流水创建命令
     * @return 支付流水保存结果
     */
    @Override
    public PaymentFlowSaveResult savePaidFlow(PaymentFlowCreateCommand command) {
        if (!isValidCommand(command)) {
            log.warn("支付流水命令非法, command={}", command);
            return PaymentFlowSaveResult.DUPLICATE_CONFLICT;
        }
        PaymentFlow paymentFlow = buildPaymentFlow(command);
        try {
            paymentFlowMapper.insert(paymentFlow);
            return PaymentFlowSaveResult.CREATED;
        } catch (DuplicateKeyException e) {
            return resolveDuplicateFlow(command);
        }
    }

    /**
     * 校验支付流水命令。
     * flowKey 是支付流水幂等和冲突判断的唯一业务键，orderSn 是重复流水归属判断的最小上下文；
     * payId、payChannel 和 payAmount 是后续渠道账单对账的核心身份字段，缺失时不能落库。
     *
     * @param command 支付流水创建命令
     * @return true=命令可进入落库流程
     */
    private boolean isValidCommand(PaymentFlowCreateCommand command) {
        return command != null
                && StringUtils.isNotBlank(command.flowKey())
                && StringUtils.isNotBlank(command.orderSn())
                && StringUtils.isNotBlank(command.payId())
                && StringUtils.isNotBlank(command.payChannel())
                && hasPositivePayAmount(command.payAmount());
    }

    /**
     * 判断支付金额是否为有效正数。
     * 支付流水进入库内后会参与订单金额和渠道账单对账，空金额或非正数会制造无法解释的对账差异。
     *
     * @param payAmount 支付金额
     * @return true=金额可作为支付成功流水保存
     */
    private boolean hasPositivePayAmount(BigDecimal payAmount) {
        return payAmount != null && payAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 解析重复支付流水归属。
     *
     * @param command 支付流水创建命令
     * @return 重复流水处理结果
     */
    private PaymentFlowSaveResult resolveDuplicateFlow(PaymentFlowCreateCommand command) {
        PaymentFlow existing = findExistingFlow(command.flowKey());
        if (isSameOrderFlow(existing, command.orderSn())) {
            // 第三方支付渠道可能重复通知，同一渠道流水已归属当前订单时按幂等成功处理。
            log.info("支付流水已存在, flowKey={}, orderSn={}", command.flowKey(), command.orderSn());
            return PaymentFlowSaveResult.DUPLICATE_SAME_ORDER;
        }
        log.error("支付流水冲突, flowKey={}, currentOrderSn={}, existingOrderSn={}",
                command.flowKey(), command.orderSn(), existing == null ? null : existing.getOrderSn());
        return PaymentFlowSaveResult.DUPLICATE_CONFLICT;
    }

    /**
     * 查询已存在的支付流水。
     * 唯一键冲突后再查询一次数据库，用于判断是支付渠道重复通知还是同一流水号被不同订单复用。
     *
     * @param flowKey 支付流水幂等键
     * @return 已存在支付流水，不存在时返回 null
     */
    private PaymentFlow findExistingFlow(String flowKey) {
        return paymentFlowMapper.selectOne(Wrappers.lambdaQuery(PaymentFlow.class)
                .eq(PaymentFlow::getFlowKey, flowKey)
                .last("limit 1"));
    }

    /**
     * 判断重复流水是否仍归属于当前订单。
     *
     * @param existing 已存在支付流水
     * @param orderSn 当前订单号
     * @return true=同订单重复通知
     */
    private boolean isSameOrderFlow(PaymentFlow existing, String orderSn) {
        return existing != null && Objects.equals(existing.getOrderSn(), orderSn);
    }

    /**
     * 构建支付流水实体。
     *
     * @param command 支付流水创建命令
     * @return 支付流水实体
     */
    private PaymentFlow buildPaymentFlow(PaymentFlowCreateCommand command) {
        Date now = new Date();
        PaymentFlow paymentFlow = new PaymentFlow();
        paymentFlow.setFlowKey(command.flowKey());
        paymentFlow.setOrderId(command.orderId());
        paymentFlow.setOrderSn(command.orderSn());
        paymentFlow.setPayId(command.payId());
        paymentFlow.setPayChannel(command.payChannel());
        paymentFlow.setPayAmount(command.payAmount());
        paymentFlow.setStatus(PaymentFlowStatusEnum.SUCCESS.getStatus());
        paymentFlow.setCreateTime(now);
        paymentFlow.setUpdateTime(now);
        return paymentFlow;
    }
}
