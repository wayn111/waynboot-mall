package com.wayn.common.design.strategy.pay.concretestrategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.wayn.common.config.AlipayConfig;
import com.wayn.common.design.strategy.pay.PayTypeEnum;
import com.wayn.common.design.strategy.pay.strategy.PayTypeInterface;
import com.wayn.common.request.OrderPayReqVO;
import com.wayn.common.response.OrderPayResVO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 支付宝H5支付策略
 */
@Slf4j
@Component
@AllArgsConstructor
public class AliH5PayStrategy implements PayTypeInterface {

    private AlipayConfig alipayConfig;

    @Override
    public OrderPayResVO pay(OrderPayReqVO reqVo) {
        // 初始化
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig.getGateway(), alipayConfig.getAppId(),
                alipayConfig.getRsaPrivateKey(), alipayConfig.getFormat(), alipayConfig.getCharset(), alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSigntype());
        // 创建API对应的request，使用手机网站支付request
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        // 在公共参数中设置回跳和通知地址
        if (StringUtils.isNotBlank(reqVo.getReturnUrl())) {
            alipayRequest.setReturnUrl(reqVo.getReturnUrl());
        }
        alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());

        // 填充业务参数
        // 必填
        // 商户订单号，需保证在商户端不重复
        String out_trade_no = reqVo.getOrderSn();
        // 销售产品码，与支付宝签约的产品码名称。目前仅支持FAST_INSTANT_TRADE_PAY
        String product_code = "FAST_INSTANT_TRADE_PAY";
        // 订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]。
        BigDecimal actualPrice = reqVo.getActualPrice();
        String total_amount = actualPrice.toString();
        // 订单标题
        String subject = reqVo.getGoodsName();

        /******必传参数******/
        JSONObject bizContent = new JSONObject();
        // 商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", out_trade_no);
        // 支付金额，最小值0.01元
        bizContent.put("total_amount", total_amount);
        // 订单标题，不可使用特殊符号
        bizContent.put("subject", subject);
        // 电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", product_code);

        alipayRequest.setBizContent(bizContent.toString());
        // 请求
        String form;
        try {
            log.info("alipay request is {}", JSON.toJSONString(alipayRequest));
            // 需要自行申请支付宝的沙箱账号、申请appID，并在配置文件中依次配置AppID、密钥、公钥，否则这里会报错。
            form = alipayClient.pageExecute(alipayRequest).getBody();// 调用SDK生成表单
            log.info("alipay form is {}", form);
        } catch (AlipayApiException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
        }
        OrderPayResVO resVO = new OrderPayResVO();
        resVO.setForm(form);
        return resVO;
    }

    @Override
    public Integer getType() {
        return PayTypeEnum.ALI_H5.getType();
    }
}
