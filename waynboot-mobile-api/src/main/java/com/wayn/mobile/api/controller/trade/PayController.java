package com.wayn.mobile.api.controller.trade;


import com.wayn.common.base.controller.BaseController;
import com.wayn.domain.api.trade.service.IPayService;
import com.wayn.domain.api.trade.request.OrderPayReqVO;
import com.wayn.domain.api.trade.response.OrderPayResVO;
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
        log.info("预支付开始, orderSn={}, payType={}", reqVO.getOrderSn(), reqVO.getPayType());
        OrderPayResVO resVO = payService.prepay(reqVO);
        log.info("预支付完成, orderSn={}, payType={}", reqVO.getOrderSn(), reqVO.getPayType());
        return R.success(resVO);
    }

}
