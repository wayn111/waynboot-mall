package com.wayn.common.design.strategy.pay.concretestrategy;

import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wayn.common.design.strategy.pay.PayTypeEnum;
import com.wayn.common.design.strategy.pay.strategy.PayTypeInterface;
import com.wayn.common.request.OrderPayReqVO;
import com.wayn.common.response.OrderPayResVO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 微信JSAPI支付策略
 */
@Slf4j
@Component
@AllArgsConstructor
public class TestPayStrategy implements PayTypeInterface {
    private WxPayService wxPayService;

    @Override
    public OrderPayResVO pay(OrderPayReqVO reqVo) {
        WxPayUnifiedOrderV3Result.JsapiResult result;
        try {
            WxPayUnifiedOrderV3Request orderRequest = new WxPayUnifiedOrderV3Request();
            WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
            payer.setOpenid(reqVo.getOpenId());
            orderRequest.setOutTradeNo(reqVo.getOrderSn());
            orderRequest.setPayer(payer);
            orderRequest.setDescription(reqVo.getGoodsName());
            WxPayUnifiedOrderV3Request.SceneInfo sceneInfo = new WxPayUnifiedOrderV3Request.SceneInfo();
            sceneInfo.setPayerClientIp(reqVo.getClientIp());
            orderRequest.setSceneInfo(sceneInfo);
            // 元转成分
            WxPayUnifiedOrderV3Request.Amount amount = new WxPayUnifiedOrderV3Request.Amount();
            // 元转成分
            BigDecimal actualPrice = reqVo.getActualPrice();
            int fee = actualPrice.multiply(new BigDecimal(100)).intValue();
            amount.setTotal(fee);
            orderRequest.setAmount(amount);
            result = wxPayService.createOrderV3(TradeTypeEnum.JSAPI, orderRequest);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
        }
        OrderPayResVO resVO = new OrderPayResVO();
        resVO.setJsapiResult(result);
        return resVO;
    }

    @Override
    public Integer getType() {
        return PayTypeEnum.WX_JSAPI.getType();
    }
}
