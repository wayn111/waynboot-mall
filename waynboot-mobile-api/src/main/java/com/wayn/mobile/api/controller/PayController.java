package com.wayn.mobile.api.controller;


import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.service.shop.IPayService;
import com.wayn.common.core.vo.OrderVO;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

/**
 * 支付接口
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("pay")
public class PayController extends BaseController {

    private IPayService payService;

    /**
     * JSAPI支付
     *
     * @param orderVO
     * @return
     */
    @PostMapping("weixin/jsapi")
    public R<WxPayMpOrderResult> jsapi(@RequestBody OrderVO orderVO) {
        return R.success(payService.prepay(orderVO.getOrderSn(), orderVO.getPayType(), MobileSecurityUtils.getUserId(), request));
    }

    /**
     * H5支付
     *
     * @param orderVO
     * @return
     */
    @PostMapping("h5pay")
    public R h5pay(@RequestBody OrderVO orderVO) throws UnsupportedEncodingException {
        return R.success(payService.h5pay(orderVO));
    }

}
