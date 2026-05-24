package com.wayn.payment.channel.refund;

import com.wayn.domain.api.trade.enums.PayTypeEnum;
import com.wayn.common.design.strategy.refund.strategy.RefundInterface;
import com.wayn.domain.api.trade.request.OrderRefundReqVO;
import com.wayn.common.model.response.OrderRefundResVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * 测试退款策略
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
