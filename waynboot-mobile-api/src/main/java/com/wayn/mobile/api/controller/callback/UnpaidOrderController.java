package com.wayn.mobile.api.controller.callback;

import com.alibaba.fastjson.JSON;
import com.wayn.common.util.R;
import com.wayn.message.core.messsage.OrderDTO;
import com.wayn.mobile.api.service.IOrderService;
import com.wayn.mobile.api.service.IOrderUnpaidService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController
@AllArgsConstructor
@RequestMapping("callback/order")
public class UnpaidOrderController {

    private IOrderUnpaidService orderUnpaidService;

    @PostMapping("unpaid")
    public R unpaid(String orderSn) {
        return orderUnpaidService.unpaid(orderSn);
    }
}
