package com.wayn.domain.trade.support.order.submit.chain;

import org.springframework.stereotype.Component;

/**
 * 下单上下文构建步骤。
 * 负责触发地址、购物车、优惠券和金额快照计算，为后续库存扣减和订单组装提供稳定上下文。
 */
@Component
public class OrderSubmitContextBuildStep implements OrderSubmitStep {

    /**
     * 返回上下文构建步骤顺序。
     *
     * @return 步骤顺序
     */
    @Override
    public int order() {
        return CONTEXT_BUILD_ORDER;
    }

    /**
     * 构建并写入下单上下文。
     *
     * @param context 下单责任链上下文
     */
    @Override
    public void execute(OrderSubmitChainContext context) {
        // 上下文里包含地址、购物车和优惠券快照，后续步骤都基于这份快照执行。
        context.buildSubmitContext();
    }
}
