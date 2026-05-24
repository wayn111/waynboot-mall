package com.wayn.common.design.strategy.refund.strategy;

import com.wayn.domain.api.trade.request.OrderRefundReqVO;
import com.wayn.common.model.response.OrderRefundResVO;

/**
 * 退款策略接口
 */
public interface RefundInterface {

    OrderRefundResVO refund(OrderRefundReqVO reqVo);

    Integer getType();
}
