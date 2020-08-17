package com.wayn.mobile.api.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.BaseController;
import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.Order;
import com.wayn.mobile.api.domain.vo.OrderVO;
import com.wayn.mobile.api.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("order")
public class OrderController extends BaseController {

    @Autowired
    private IOrderService iOrderService;

    @GetMapping("list")
    public R list(@RequestParam(defaultValue = "0") Integer showType) {
        Page<Order> page = getPage();
        return iOrderService.selectListPage(page, showType);
    }

    @PostMapping("statusCount")
    public R statusCount() {
        return iOrderService.statusCount();
    }


    @PostMapping("submit")
    public R submit(@RequestBody OrderVO orderVO) {
        return iOrderService.submit(orderVO);
    }

    @PostMapping("info")
    public R info(@RequestBody OrderVO orderVO) {
        return R.success().add("data", iOrderService.getById(orderVO.getOrderId()));
    }

    @PostMapping("prepay")
    public R prepay(@RequestBody OrderVO orderVO) {
        return iOrderService.prepay(orderVO.getOrderId(), request);
    }

    @PostMapping("h5pay")
    public R h5pay(@RequestBody OrderVO orderVO) {
        return iOrderService.h5pay(orderVO.getOrderId(), request);
    }

    @PostMapping("payNotify")
    public R payNotify(HttpServletRequest request, HttpServletResponse response) {
        return iOrderService.payNotify(request, response);
    }

}
