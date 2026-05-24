package com.wayn.domain.trade.support.order.submit.chain;

import com.wayn.domain.trade.support.order.OrderAssemblerSupport;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 订单实体组装步骤。
 * 将责任链上下文中的 DTO 和金额快照转换为待落库订单主表对象，供单笔或批量持久化复用。
 */
@Component
@AllArgsConstructor
public class OrderSubmitEntityBuildStep implements OrderSubmitStep {

    private final OrderAssemblerSupport orderAssemblerSupport;

    /**
     * 返回订单实体组装步骤顺序。
     *
     * @return 步骤顺序
     */
    @Override
    public int order() {
        return ORDER_BUILD_ORDER;
    }

    /**
     * 组装订单主表对象并写入责任链上下文。
     *
     * @param context 下单责任链上下文
     */
    @Override
    public void execute(OrderSubmitChainContext context) {
        // 订单实体只组装一次，单笔直接落库，批量则暂存在上下文里等待统一批量写入。
        context.setOrder(orderAssemblerSupport.buildOrder(context.getOrderDTO(), context.getSubmitContext()));
    }
}
