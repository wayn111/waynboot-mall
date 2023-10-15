package com.wayn.mobile.api.controller;


import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.vo.OrderVO;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IPayService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

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
    public R jsapi(@RequestBody OrderVO orderVO) {
        return payService.prepay(orderVO.getOrderSn(), orderVO.getPayType(), request);
    }

    /**
     * H5支付
     *
     * @param orderVO
     * @return
     */
    @PostMapping("h5pay")
    public R h5pay(@RequestBody OrderVO orderVO) throws UnsupportedEncodingException {
        return payService.h5pay(orderVO);
    }

}
