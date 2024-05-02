package com.wayn.common.design.strategy.refund.concretestrategy;

import com.wayn.common.design.strategy.pay.PayTypeEnum;
import com.wayn.common.design.strategy.refund.strategy.RefundInterface;
import com.wayn.common.request.OrderRefundReqVO;
import com.wayn.common.response.OrderRefundResVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * 支付宝H5支付策略
 */
@Slf4j
@Component
@AllArgsConstructor
public class TestRefundStrategy implements RefundInterface {

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    @Override
    public OrderRefundResVO refund(OrderRefundReqVO reqVo) {
        return new OrderRefundResVO();
    }

    @Override
    public Integer getType() {
        return PayTypeEnum.TEST.getType();
    }
}
