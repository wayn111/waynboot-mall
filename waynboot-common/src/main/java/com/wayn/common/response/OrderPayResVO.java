package com.wayn.common.response;

import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import lombok.Data;

/**
 * 下单请求结果
 */
@Data
public class OrderPayResVO {

    /**
     * 支付宝h5支付返回
     */
    private String form;

    /**
     * 微信h5支付返回
     */
    private String mwebUrl;

    /**
     * 易支付前台跳转url支付链接
     */
    private String epayurl;

    /**
     * 微信jsapi支付返回
     */
    private WxPayUnifiedOrderV3Result.JsapiResult jsapiResult;

}
