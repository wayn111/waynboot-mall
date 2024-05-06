package com.wayn.mobile.api.controller;


import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.service.shop.IPayService;
import com.wayn.common.request.OrderPayReqVO;
import com.wayn.common.response.OrderPayResVO;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付接口
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("pay")
public class PayController extends BaseController {

    private IPayService payService;

    /**
     * 商城统一支付下单接口
     *
     * @param reqVO
     * @return
     */
    @PostMapping("prepay")
    public R<OrderPayResVO> prepay(@RequestBody @Validated OrderPayReqVO reqVO) {
        log.info("order prepay reqVO is {}", reqVO);
        return R.success(payService.prepay(reqVO));
    }

}
