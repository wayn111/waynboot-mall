package com.wayn.common.core.service.shop.support.order.submit.chain;

import com.wayn.common.core.service.shop.support.order.OrderStockSupport;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 下单库存冻结步骤。
 * 在订单对象组装和落库前执行 MySQL 条件冻结，失败时依赖外层单笔事务回滚本次订单副作用。
 */
@Component
@AllArgsConstructor
public class OrderSubmitStockReduceStep implements OrderSubmitStep {

    private final OrderStockSupport orderStockSupport;

    /**
     * 返回库存冻结步骤顺序。
     *
     * @return 步骤顺序
     */
    @Override
    public int order() {
        return STOCK_REDUCE_ORDER;
    }

    /**
     * 按下单上下文中的购物车快照冻结库存。
     *
     * @param context 下单责任链上下文
     */
    @Override
    public void execute(OrderSubmitChainContext context) {
        // 只使用上下文里的购物车快照冻结库存，避免后续购物车变化影响当前订单的库存口径。
        orderStockSupport.freezeStock(context.getOrderDTO().getOrderSn(), context.getSubmitContext().checkedGoodsList());
    }
}
