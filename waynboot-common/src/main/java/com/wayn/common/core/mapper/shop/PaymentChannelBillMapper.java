package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.common.core.entity.shop.PaymentChannelBill;

/**
 * 支付渠道账单 Mapper。
 * 渠道账单导入后由对账服务统一读取并和内部支付流水比对。
 */
public interface PaymentChannelBillMapper extends BaseMapper<PaymentChannelBill> {
}
