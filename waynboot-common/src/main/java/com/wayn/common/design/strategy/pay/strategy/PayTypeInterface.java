package com.wayn.common.design.strategy.pay.strategy;

import com.wayn.common.model.request.OrderPayReqVO;
import com.wayn.common.model.response.OrderPayResVO;

/**
 * 支付策略接口
 */
public interface PayTypeInterface {

    OrderPayResVO pay(OrderPayReqVO reqVo);

    Integer getType();
}
