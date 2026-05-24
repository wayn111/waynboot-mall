package com.wayn.domain.api.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.domain.api.trade.entity.PaymentFlow;

/**
 * 支付流水 Mapper。
 * 只负责支付流水基础持久化，渠道流水唯一幂等由数据库唯一键和 PaymentFlowService 共同保证。
 */
public interface PaymentFlowMapper extends BaseMapper<PaymentFlow> {
}
