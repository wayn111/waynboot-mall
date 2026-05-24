package com.wayn.payment.channel.pay;

import com.wayn.common.config.EpayConfig;
import com.wayn.domain.api.trade.enums.PayTypeEnum;
import com.wayn.common.design.strategy.pay.strategy.PayTypeInterface;
import com.wayn.domain.api.trade.request.OrderPayReqVO;
import com.wayn.domain.api.trade.response.OrderPayResVO;
import com.wayn.payment.channel.epay.Epayapi;
import com.wayn.payment.channel.epay.EpayPrepareRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 易支付策略
 */
@Slf4j
@Component
@AllArgsConstructor
public class EPayWxPayStrategy implements PayTypeInterface {

    private Epayapi epayapi;

    private EpayConfig epayConfig;

    @Override
    public OrderPayResVO pay(OrderPayReqVO reqVo) {
        // 构造要请求的参数数组，无需改动
        EpayPrepareRequest request = new EpayPrepareRequest(epayConfig.getPid(), "wxpay", epayConfig.getNotifyUrl(),
                reqVo.getReturnUrl(), reqVo.getOrderSn(), reqVo.getGoodsName(), reqVo.getActualPrice().toString(),
                reqVo.getClientIp());
        String submit = epayapi.submit(request);
        OrderPayResVO orderPayResVO = new OrderPayResVO();
        orderPayResVO.setEpayurl(submit);
        return orderPayResVO;
    }

    @Override
    public Integer getType() {
        return PayTypeEnum.EPAY_WECHAT.getType();
    }
}
