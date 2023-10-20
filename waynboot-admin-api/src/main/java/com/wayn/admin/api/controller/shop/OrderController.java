package com.wayn.admin.api.controller.shop;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.wayn.admin.framework.config.properties.ExpressProperties;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.vo.ShipVO;
import com.wayn.common.core.service.shop.IOrderService;
import com.wayn.common.util.R;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;


/**
 * 订单管理
 *
 * @author wayn
 * @since 2020-07-06
 */
@RestController
@AllArgsConstructor
@RequestMapping("shop/order")
public class OrderController extends BaseController {

    private ExpressProperties expressProperties;

    private IOrderService iOrderService;

    @PreAuthorize("@ss.hasPermi('shop:order:list')")
    @GetMapping("list")
    public R list(Order order) {
        Page<Order> page = getPage();
        return R.success().add("page", iOrderService.listPage(page, order));
    }

    @PreAuthorize("@ss.hasPermi('shop:order:info')")
    @GetMapping("{orderId}")
    public R info(@PathVariable Long orderId) {
        return iOrderService.detail(orderId);
    }

    @PreAuthorize("@ss.hasPermi('shop:order:delete')")
    @DeleteMapping("{orderId}")
    public R deleteOrder(@PathVariable Long orderId) {
        return R.result(iOrderService.removeById(orderId));
    }

    @PreAuthorize("@ss.hasPermi('shop:order:refund')")
    @PostMapping("refund/{orderId}")
    public R refund(@PathVariable Long orderId) throws UnsupportedEncodingException, WxPayException, AlipayApiException {
        return iOrderService.refund(orderId);
    }

    @PostMapping("listChannel")
    public R channel() {
        return R.success().add("data", expressProperties.getVendors());
    }

    @PreAuthorize("@ss.hasPermi('shop:order:ship')")
    @PostMapping("ship")
    public R ship(@RequestBody  ShipVO shipVO) throws UnsupportedEncodingException {
        return iOrderService.ship(shipVO);
    }
}

