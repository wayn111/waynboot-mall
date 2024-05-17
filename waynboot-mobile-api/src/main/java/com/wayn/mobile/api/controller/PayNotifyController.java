package com.wayn.mobile.api.controller;


import com.alibaba.fastjson.JSONObject;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.service.shop.IPayService;
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
        log.info("微信paySuccess通知数据记录：req：{}", JSONObject.toJSONString(request.getParameterMap()));
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
        log.info("支付宝paySuccess通知数据记录：req: {}", JSONObject.toJSONString(request.getParameterMap()));
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
        log.info("易支付paySuccess通知数据记录：req：{}", JSONObject.toJSONString(request.getParameterMap()));
        return payService.epayPayNotify(request, response);
    }
}
