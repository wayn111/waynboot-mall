package com.wayn.mobile.api.controller;


import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.service.shop.IMobileOrderService;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 退款接口
 *
 * @author wayn
 * @since 2024/1/15
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("order/refund")
public class OrderRefundController extends BaseController {

    private IMobileOrderService mobileOrderService;

    /**
     * 申请退款
     *
     * @param orderId 订单id
     * @return R
     */
    @PostMapping("{orderId}")
    public R<Boolean> refund(@PathVariable Long orderId) {
        mobileOrderService.refund(orderId);
        return R.success();
    }

}
