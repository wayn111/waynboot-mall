package com.wayn.mobile.api.controller;


import com.alipay.api.AlipayApiException;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.vo.OrderVO;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IPayService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("order/refund")
public class OrderRefundController extends BaseController {

    private IPayService payService;

    @PostMapping("{orderId}")
    public R refund(@PathVariable Long orderId) throws UnsupportedEncodingException, AlipayApiException, WxPayException {
        return payService.refund(orderId);
    }
}
