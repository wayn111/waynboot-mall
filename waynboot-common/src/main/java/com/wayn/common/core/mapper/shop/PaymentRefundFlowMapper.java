package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.common.core.entity.shop.PaymentRefundFlow;

/**
 * 支付退款流水 Mapper。
 * 用于日终对账读取退款流水并校验订单退款状态。
 */
public interface PaymentRefundFlowMapper extends BaseMapper<PaymentRefundFlow> {
}
