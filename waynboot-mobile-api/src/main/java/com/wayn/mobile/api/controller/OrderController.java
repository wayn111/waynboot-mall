package com.wayn.mobile.api.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.vo.OrderVO;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IMobileOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("order")
public class OrderController extends BaseController {

    private IMobileOrderService iMobileOrderService;

    @GetMapping("detail/{orderSn}")
    public R detail(@PathVariable String orderSn) {
        return iMobileOrderService.getOrderDetailByOrderSn(orderSn);
    }

    @GetMapping("list")
    public R list(@RequestParam(defaultValue = "0") Integer showType) {
        Page<Order> page = getPage();
        return iMobileOrderService.selectListPage(page, showType);
    }

    @PostMapping("info")
    public R info(@RequestBody OrderVO orderVO) {
        return R.success().add("data", iMobileOrderService.getById(orderVO.getOrderSn()));
    }

    @PostMapping("statusCount")
    public R statusCount() {
        return iMobileOrderService.statusCount();
    }

    @PostMapping("submit")
    public R submit(@RequestBody OrderVO orderVO) throws Exception {
        return iMobileOrderService.asyncSubmit(orderVO);
    }

    @GetMapping("searchResult/{orderSn}")
    public R searchResult(@PathVariable String orderSn) {
        return iMobileOrderService.searchResult(orderSn);
    }

    @PostMapping("cancel/{orderId}")
    public R cancel(@PathVariable Long orderId) {
        return iMobileOrderService.cancel(orderId);
    }

    @PostMapping("confirm/{orderId}")
    public R confirm(@PathVariable Long orderId) {
        return iMobileOrderService.confirm(orderId);
    }

    @PostMapping("delete/{orderId}")
    public R delete(@PathVariable Long orderId) {
        return iMobileOrderService.delete(orderId);
    }

}
