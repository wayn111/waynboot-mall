package com.wayn.common.design.strategy.refund.concretestrategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.wayn.common.config.AlipayConfig;
import com.wayn.common.design.strategy.pay.PayTypeEnum;
import com.wayn.common.design.strategy.refund.strategy.RefundInterface;
import com.wayn.common.request.OrderRefundReqVO;
import com.wayn.common.response.OrderRefundResVO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.OrderSnGenUtil;
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
public class AliH5RefundStrategy implements RefundInterface {

    private AlipayConfig alipayConfig;
    private OrderSnGenUtil orderSnGenUtil;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    @Override
    public OrderRefundResVO refund(OrderRefundReqVO reqVo) {
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig.getGateway(), alipayConfig.getAppId(),
                alipayConfig.getRsaPrivateKey(), alipayConfig.getFormat(), alipayConfig.getCharset(), alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSigntype());
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        String refundSn = orderSnGenUtil.generateRefundOrderSn();
        bizContent.put("trade_no", reqVo.getPayId());
        bizContent.put("refund_amount", reqVo.getRefundMoney());
        bizContent.put("out_request_no", refundSn);
        bizContent.put("refund_reason", reqVo.getRefundReason());

        request.setBizContent(bizContent.toString());
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
            log.info("response:{}", JSON.toJSONString(response));
            if (!response.isSuccess()) {
                throw new BusinessException(ReturnCodeEnum.ORDER_REFUND_ERROR);
            }
        } catch (AlipayApiException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return new OrderRefundResVO();
    }

    @Override
    public Integer getType() {
        return PayTypeEnum.ALI_H5.getType();
    }
}
