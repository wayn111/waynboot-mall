package com.wayn.mobile.api.controller.callback;

import com.wayn.common.core.service.shop.IOrderUnpaidService;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 未支付订单取消回调
 */
@RestController
@AllArgsConstructor
@RequestMapping("callback/order")
public class UnpaidOrderController {

    private IOrderUnpaidService orderUnpaidService;

    /**
     * 未支付订单取消
     *
     * @param orderSn 订单编号
     * @return R
     */
    @PostMapping("unpaid")
    public R unpaid(String orderSn) {
        orderUnpaidService.unpaid(orderSn);
        return R.success();
    }

}
