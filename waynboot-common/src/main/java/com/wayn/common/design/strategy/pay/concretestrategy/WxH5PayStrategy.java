package com.wayn.common.design.strategy.pay.concretestrategy;

import cn.hutool.core.net.NetUtil;
import com.alibaba.fastjson.JSONObject;
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
 * 微信H5支付策略
 */
@Slf4j
@Component
@AllArgsConstructor
public class WxH5PayStrategy implements PayTypeInterface {

    private WxPayService wxPayService;

    @Override
    public OrderPayResVO pay(OrderPayReqVO reqVo) {
        WxPayUnifiedOrderV3Result result;
        try {
            WxPayUnifiedOrderV3Request orderRequest = new WxPayUnifiedOrderV3Request();
            orderRequest.setOutTradeNo(reqVo.getOrderSn());
            WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
            orderRequest.setPayer(payer);
            orderRequest.setDescription(reqVo.getGoodsName());
            WxPayUnifiedOrderV3Request.SceneInfo sceneInfo = new WxPayUnifiedOrderV3Request.SceneInfo();
            sceneInfo.setPayerClientIp(NetUtil.getLocalhost().getHostAddress());
            orderRequest.setSceneInfo(sceneInfo);
            // 元转成分
            WxPayUnifiedOrderV3Request.Amount amount = new WxPayUnifiedOrderV3Request.Amount();
            // 元转成分
            BigDecimal actualPrice = reqVo.getActualPrice();
            int fee = actualPrice.multiply(new BigDecimal(100)).intValue();
            amount.setTotal(fee);
            orderRequest.setAmount(amount);
            log.info("weixin h5 pay request is {}", orderRequest);
            result = wxPayService.unifiedOrderV3(TradeTypeEnum.H5, orderRequest);
            log.info("weixin h5 pay result:{}", result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
        }
        OrderPayResVO resVO = new OrderPayResVO();
        resVO.setMwebUrl(result.getH5Url());
        return resVO;
    }

    @Override
    public Integer getType() {
        return PayTypeEnum.WX_H5.getType();
    }
}
