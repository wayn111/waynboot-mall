package com.wayn.domain.trade.service.impl;

import com.wayn.domain.api.trade.service.IPayService;
import com.wayn.domain.trade.support.payment.PaymentCallbackSupport;
import com.wayn.domain.trade.support.payment.PaymentPrepareSupport;
import com.wayn.domain.api.trade.request.OrderPayReqVO;
import com.wayn.domain.api.trade.response.OrderPayResVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 支付服务外观层。
 * 对外保持原有支付接口不变，内部把预支付和回调处理分别委托给专门的支撑服务。
 */
@Service
@AllArgsConstructor
public class PayServiceImpl implements IPayService {

    private final PaymentPrepareSupport paymentPrepareSupport;
    private final PaymentCallbackSupport paymentCallbackSupport;

    /**
     * 委托执行预支付。
     *
     * @param reqVO 支付请求
     * @return 预支付结果
     */
    @Override
    public OrderPayResVO prepay(OrderPayReqVO reqVO) {
        return paymentPrepareSupport.prepay(reqVO);
    }

    /**
     * 委托处理微信支付回调。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @return 回调响应
     */
    @Override
    public String wxPayNotify(HttpServletRequest request, HttpServletResponse response) {
        return paymentCallbackSupport.wxPayNotify(request, response);
    }

    /**
     * 委托处理支付宝支付回调。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @return 回调响应
     */
    @Override
    public String aliPayNotify(HttpServletRequest request, HttpServletResponse response) {
        return paymentCallbackSupport.aliPayNotify(request, response);
    }

    /**
     * 委托处理易支付回调。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @return 回调响应
     */
    @Override
    public String epayPayNotify(HttpServletRequest request, HttpServletResponse response) {
        return paymentCallbackSupport.epayPayNotify(request, response);
    }
}
