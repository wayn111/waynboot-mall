package com.wayn.mobile.api.service;

import com.alipay.api.AlipayApiException;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.wayn.common.core.domain.vo.OrderVO;
import com.wayn.common.util.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;

/**
 * 订单表 服务类
 *
 * @author wayn
 * @since 2020-08-11
 */
public interface IPayService {

    /**
     * 付款订单的预支付会话标识
     * <p>
     * 1. 检测当前订单是否能够付款
     * 2. 微信商户平台返回支付订单ID
     * 3. 设置订单付款状态
     *
     * @param orderSn 订单编号
     * @param request 请求
     * @return r
     */
    R prepay(String orderSn, Integer payType, HttpServletRequest request);

    /**
     * 微信H5支付
     *
     * @param orderVO 订单VO
     * @return r
     */
    R h5pay(OrderVO orderVO) throws UnsupportedEncodingException;

    /**
     * 订单退款
     * <p>
     * 1. 检测当前订单是否可以退款；
     * 2. 更改订单状态为已退款。
     *
     * @param orderId 订单ID
     * @return r
     */
    R refund(Long orderId) throws UnsupportedEncodingException, AlipayApiException, WxPayException;

    String wxPayNotify(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException;

    String aliPayNotify(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException, UnsupportedEncodingException;
}
