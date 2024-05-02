package com.wayn.common.design.strategy.pay.concretestrategy;

import com.wayn.common.design.strategy.pay.PayTypeEnum;
import com.wayn.common.design.strategy.pay.strategy.PayTypeInterface;
import com.wayn.common.request.OrderPayReqVO;
import com.wayn.common.response.OrderPayResVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 支付宝H5支付策略
 */
@Slf4j
@Component
@AllArgsConstructor
public class AliH5PayStrategy implements PayTypeInterface {

    @Override
    public OrderPayResVO pay(OrderPayReqVO reqVo) {
        return new OrderPayResVO();
    }

    @Override
    public Integer getType() {
        return PayTypeEnum.TEST.getType();
    }
}
