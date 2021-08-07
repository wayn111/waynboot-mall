package com.wayn.mobile.api.controller;


import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.vo.OrderVO;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
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

    @PostMapping("info")
    public R info(@RequestBody OrderVO orderVO) {
        return R.success().add("data", iOrderService.getById(orderVO.getOrderSn()));
    }

    @PostMapping("statusCount")
    public R statusCount() {
        return iOrderService.statusCount();
    }

    @PostMapping("submit")
    public R submit(@RequestBody OrderVO orderVO) {
        return iOrderService.asyncSubmit(orderVO);
    }

    /**
     * JSAPI支付
     *
     * @param orderVO
     * @return
     */
    @PostMapping("prepay")
    public R prepay(@RequestBody OrderVO orderVO) {
        return iOrderService.prepay(orderVO.getOrderSn(), orderVO.getPayType(), request);
    }

    /**
     * H5支付
     *
     * @param orderVO
     * @return
     */
    @PostMapping("h5pay")
    public R h5pay(@RequestBody OrderVO orderVO) {
        return iOrderService.h5pay(orderVO.getOrderSn(), orderVO.getPayType(), request);
    }

    @PostMapping("wxPayNotify")
    public void wxPayNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("微信paySuccess通知数据记录：req：{}", JSONObject.toJSONString(request.getParameterMap()));
        iOrderService.wxPayNotify(request, response);
    }

    @PostMapping("aliPayNotify")
    public void aliPayNotify(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException {
        log.info("支付宝paySuccess通知数据记录：req: {}", JSONObject.toJSONString(request.getParameterMap()));
        iOrderService.aliPayNotify(request, response);
    }

    @GetMapping("searchResult/{orderSn}")
    public R searchResult(@PathVariable String orderSn) {
        return iOrderService.searchResult(orderSn);
    }

    @PostMapping("cancel/{orderId}")
    public R cancel(@PathVariable Long orderId) {
        return iOrderService.cancel(orderId);
    }

    @PostMapping("refund/{orderId}")
    public R refund(@PathVariable Long orderId) {
        return iOrderService.refund(orderId);
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
