package com.wayn.mobile.api.controller.callback;

import com.alibaba.fastjson.JSON;
import com.wayn.common.util.R;
import com.wayn.message.core.messsage.OrderDTO;
import com.wayn.mobile.api.service.IOrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController
@AllArgsConstructor
@RequestMapping("callback/order")
public class SubmitOrderController {

    private IOrderService iOrderService;

    @PostMapping("submit")
    public R submit(String order) throws UnsupportedEncodingException {
        OrderDTO orderDTO = JSON.parseObject(order, OrderDTO.class);
        return iOrderService.submit(orderDTO);
    }
}
