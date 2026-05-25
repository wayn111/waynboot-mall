package com.wayn.domain.api.trade.service;

import com.wayn.domain.api.trade.enums.PaymentFlowSaveResult;

/**
 * 支付流水服务接口。
 * 契约层只描述支付流水幂等保存能力，具体落库实现放在交易领域模块。
 */
public interface PaymentFlowService {

    /**
     * 保存支付成功流水。
     *
     * @param command 支付流水创建命令
     * @return 支付流水保存结果
     */
    PaymentFlowSaveResult savePaidFlow(PaymentFlowCreateCommand command);
}
