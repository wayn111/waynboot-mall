package com.wayn.mobile.api.controller.trade;

import com.wayn.common.base.controller.BaseController;
import com.wayn.domain.api.trade.service.IPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付成功回调接口
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("pay/callback")
public class PayNotifyController extends BaseController {

    private IPayService payService;

    /**
     * 微信支付回调
     *
     * @param request
     * @param response
     * @return string
     */
    @RequestMapping("wxPayNotify")
    public String wxPayNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("收到微信支付回调, summary={}", summarizeCallback(request));
        return payService.wxPayNotify(request, response);
    }

    /**
     * 支付宝支付回调
     *
     * @param request
     * @param response
     * @return string
     */
    @RequestMapping("aliPayNotify")
    public String aliPayNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("收到支付宝支付回调, summary={}", summarizeCallback(request));
        return payService.aliPayNotify(request, response);
    }


    /**
     * 易支付回调
     *
     * @param request
     * @param response
     * @return string
     */
    @RequestMapping("epayNotify")
    public String epayNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("收到易支付回调, summary={}", summarizeCallback(request));
        return payService.epayPayNotify(request, response);
    }

    /**
     * 生成回调日志摘要，避免直接打印完整签名参数。
     *
     * @param request HTTP 请求
     * @return 日志摘要
     */
    private String summarizeCallback(HttpServletRequest request) {
        String orderSn = request.getParameter("out_trade_no");
        String tradeNo = request.getParameter("trade_no");
        String transactionId = request.getParameter("transaction_id");
        return String.format("path=%s, orderSn=%s, tradeNo=%s, transactionId=%s",
                request.getRequestURI(), orderSn, tradeNo, transactionId);
    }
}
