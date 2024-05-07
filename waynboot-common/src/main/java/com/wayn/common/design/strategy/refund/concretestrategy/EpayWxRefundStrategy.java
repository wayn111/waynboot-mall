package com.wayn.common.design.strategy.refund.concretestrategy;

import com.wayn.common.config.EpayConfig;
import com.wayn.common.design.strategy.pay.PayTypeEnum;
import com.wayn.common.design.strategy.refund.strategy.RefundInterface;
import com.wayn.common.request.OrderRefundReqVO;
import com.wayn.common.response.OrderRefundResVO;
import com.wayn.common.wapper.epay.Epayapi;
import com.wayn.common.wapper.epay.requet.EpayRefundRequest;
import com.wayn.util.util.OrderSnGenUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * 易支付-微信退款策略
 */
@Slf4j
@Component
@AllArgsConstructor
public class EpayWxRefundStrategy implements RefundInterface {

    private Epayapi epayapi;
    private OrderSnGenUtil orderSnGenUtil;
    private EpayConfig epayConfig;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    @Override
    public OrderRefundResVO refund(OrderRefundReqVO reqVo) {
        String refundSn = orderSnGenUtil.generateRefundOrderSn();
        EpayRefundRequest request = new EpayRefundRequest(epayConfig.getPid(), epayConfig.getKey(), reqVo.getOrderSn(),
                reqVo.getRefundMoney().toString());
        String refund = epayapi.refund(request);
        return new OrderRefundResVO();
    }

    @Override
    public Integer getType() {
        return PayTypeEnum.EPAY_WECHAT.getType();
    }
}
