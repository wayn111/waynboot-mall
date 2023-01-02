package com.wayn.mobile.api.controller.message;

import com.alibaba.fastjson.JSON;
import com.wayn.common.util.R;
import com.wayn.message.core.messsage.OrderDTO;
import com.wayn.mobile.api.service.IOrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("message/order")
public class SubmitOrderController {

    private IOrderService iOrderService;

    @PostMapping("submit")
    public R submit(String order) {
        OrderDTO orderDTO = JSON.parseObject(order, OrderDTO.class);
        return iOrderService.submit(orderDTO);
    }
}
