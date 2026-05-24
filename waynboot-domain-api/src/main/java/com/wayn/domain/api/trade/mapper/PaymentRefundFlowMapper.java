package com.wayn.domain.api.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.domain.api.trade.entity.PaymentRefundFlow;

/**
 * 支付退款流水 Mapper。
 * 用于日终对账读取退款流水并校验订单退款状态。
 */
public interface PaymentRefundFlowMapper extends BaseMapper<PaymentRefundFlow> {
}
