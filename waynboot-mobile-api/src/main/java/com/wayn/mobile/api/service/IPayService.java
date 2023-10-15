package com.wayn.mobile.api.service;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.vo.OrderVO;
import com.wayn.common.util.R;
import com.wayn.message.core.dto.OrderDTO;
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
     * 微信H5支付
     *
     * @param orderSn 订单编号
     * @param request 请求
     * @return r
     */
    R h5pay(String orderSn, Integer payType, HttpServletRequest request) throws UnsupportedEncodingException;

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

    String wxPayNotify(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException;

    String aliPayNotify(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException, UnsupportedEncodingException;
}
