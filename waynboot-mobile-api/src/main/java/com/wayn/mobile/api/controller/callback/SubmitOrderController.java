package com.wayn.mobile.api.controller.callback;

import com.alibaba.fastjson.JSON;
import com.wayn.common.util.R;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.mobile.api.service.IMobileOrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController
@AllArgsConstructor
@RequestMapping("callback/order")
public class SubmitOrderController {

    private IMobileOrderService iMobileOrderService;

    @PostMapping("submit")
    public R submit(String order) throws UnsupportedEncodingException {
        OrderDTO orderDTO = JSON.parseObject(order, OrderDTO.class);
        return iMobileOrderService.submit(orderDTO);
    }
}
