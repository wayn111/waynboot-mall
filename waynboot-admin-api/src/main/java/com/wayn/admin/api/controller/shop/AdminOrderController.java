package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.framework.config.properties.ExpressProperties;
import com.wayn.common.base.BaseController;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.service.shop.IAdminOrderService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("shop/order")
public class AdminOrderController extends BaseController {

    @Autowired
    private ExpressProperties expressProperties;

    @Autowired
    private IAdminOrderService iAdminOrderService;

    @GetMapping("list")
    public R list(Order order) {
        Page<Order> page = getPage();
        return R.success().add("page", iAdminOrderService.selectListPage(page, order));
    }

    @GetMapping("{orderId}")
    public R info(@PathVariable Long orderId) {
        return iAdminOrderService.detail(orderId);
    }

    @DeleteMapping("{orderId}")
    public R deleteOrder(@PathVariable Long orderId) {
        return R.result(iAdminOrderService.removeById(orderId));
    }

    @PostMapping("refund/{orderId}")
    public R refund(@PathVariable Long orderId) {
        return iAdminOrderService.refund(orderId);
    }

    @PostMapping("listChannel")
    public R channel() {
        return R.success().add("data", expressProperties.getVendors());
    }

    @PostMapping("ship/{orderId}")
    public R ship(@PathVariable Long orderId) {
        return iAdminOrderService.ship(orderId);
    }
}

