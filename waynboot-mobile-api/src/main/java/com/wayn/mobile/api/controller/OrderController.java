package com.wayn.mobile.api.controller;


import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.vo.OrderVO;
import com.wayn.mobile.api.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private IOrderService iOrderService;

    @PostMapping("submit")
    public R submit(@RequestBody OrderVO orderVO) {
        return iOrderService.addOrder(orderVO);
    }

}
