package com.wayn.mobile.api.controller;


import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.vo.OrderVO;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("order")
public class OrderController extends BaseController {

    private IOrderService iOrderService;

    @GetMapping("detail/{orderSn}")
    public R detail(@PathVariable String orderSn) {
        return iOrderService.getOrderDetailByOrderSn(orderSn);
    }

    @GetMapping("list")
    public R list(@RequestParam(defaultValue = "0") Integer showType) {
        Page<Order> page = getPage();
        return iOrderService.selectListPage(page, showType);
    }

    @PostMapping("info")
    public R info(@RequestBody OrderVO orderVO) {
        return R.success().add("data", iOrderService.getById(orderVO.getOrderSn()));
    }

    @PostMapping("statusCount")
    public R statusCount() {
        return iOrderService.statusCount();
    }

    @PostMapping("submit")
    public R submit(@RequestBody OrderVO orderVO) throws Exception {
        return iOrderService.asyncSubmit(orderVO);
    }

    @GetMapping("searchResult/{orderSn}")
    public R searchResult(@PathVariable String orderSn) {
        return iOrderService.searchResult(orderSn);
    }

    @PostMapping("cancel/{orderId}")
    public R cancel(@PathVariable Long orderId) {
        return iOrderService.cancel(orderId);
    }

    @PostMapping("confirm/{orderId}")
    public R confirm(@PathVariable Long orderId) {
        return iOrderService.confirm(orderId);
    }

    @PostMapping("delete/{orderId}")
    public R delete(@PathVariable Long orderId) {
        return iOrderService.delete(orderId);
    }

}
