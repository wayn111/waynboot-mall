package com.wayn.domain.api.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.domain.api.trade.entity.PaymentChannelBill;

/**
 * 支付渠道账单 Mapper。
 * 渠道账单导入后由对账服务统一读取并和内部支付流水比对。
 */
public interface PaymentChannelBillMapper extends BaseMapper<PaymentChannelBill> {
}
