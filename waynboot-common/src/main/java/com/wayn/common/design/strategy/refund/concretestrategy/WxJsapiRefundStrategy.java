package com.wayn.common.design.strategy.refund.concretestrategy;

import com.alibaba.fastjson.JSON;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
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

import java.math.BigDecimal;

/**
 * 微信JSAPI支付策略
 */
@Slf4j
@Component
@AllArgsConstructor
public class WxJsapiRefundStrategy implements RefundInterface {
    private WxPayService wxPayService;
    private OrderSnGenUtil orderSnGenUtil;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    @Override
    public OrderRefundResVO refund(OrderRefundReqVO reqVo) {
        WxPayRefundV3Request refundV3Request = new WxPayRefundV3Request();
        String refundSn = orderSnGenUtil.generateRefundOrderSn();
        refundV3Request.setTransactionId(reqVo.getPayId());
        refundV3Request.setOutRefundNo(refundSn);
        refundV3Request.setReason(reqVo.getRefundReason());
        WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
        amount.setRefund(reqVo.getRefundMoney().multiply(new BigDecimal("100")).intValue());
        amount.setCurrency("CNY");
        amount.setTotal(reqVo.getRefundMoney().multiply(new BigDecimal("100")).intValue());
        refundV3Request.setAmount(amount);
        WxPayRefundV3Result refundV3Result;
        try {
            refundV3Result = wxPayService.refundV3(refundV3Request);
            log.info("WxJsapiRefundStrategy refund response is {}", JSON.toJSONString(refundV3Result));
            String status = refundV3Result.getStatus();
            if (!"SUCCESS".equals(status)) {
                throw new BusinessException(ReturnCodeEnum.ORDER_REFUND_ERROR);
            }
        } catch (WxPayException e) {
            throw new RuntimeException(e);
        }
        return new OrderRefundResVO();
    }

    @Override
    public Integer getType() {
        return PayTypeEnum.WX_JSAPI.getType();
    }
}
