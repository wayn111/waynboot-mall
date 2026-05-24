package com.wayn.domain.trade.support.order.submit.chain;

import com.wayn.domain.inventory.support.OrderStockSupport;
import com.wayn.domain.inventory.support.RedisStockPreDeductSupport;
import com.wayn.domain.inventory.support.RedisStockReservation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import com.wayn.domain.inventory.support.OrderStockSupport;
import com.wayn.domain.inventory.support.RedisStockPreDeductSupport;
import com.wayn.domain.inventory.support.RedisStockSnapshotSupport;
import com.wayn.domain.inventory.support.RedisStockBucketRouter;
import com.wayn.domain.inventory.support.RedisStockKeySupport;
import com.wayn.domain.inventory.support.RedisStockReservation;

/**
 * 下单库存冻结步骤。
 * 先通过 Redis Lua 预占削峰，再执行 MySQL 条件冻结；Redis 只是并发闸门，最终一致性仍以 MySQL 和库存流水为准。
 */
@Component
@AllArgsConstructor
public class OrderSubmitStockReduceStep implements OrderSubmitStep {

    private final OrderStockSupport orderStockSupport;
    private final RedisStockPreDeductSupport redisStockPreDeductSupport;

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
        RedisStockReservation reservation = redisStockPreDeductSupport.preDeduct(context.getOrderDTO().getOrderSn(),
                context.getSubmitContext().checkedGoodsList());
        try {
            orderStockSupport.freezeStock(context.getOrderDTO().getOrderSn(),
                    context.getSubmitContext().checkedGoodsList());
        } finally {
            // Redis 预占只保护进入 MySQL 条件冻结前的并发窗口，库存真实扣减成功与否由 MySQL 结果决定。
            reservation.release();
        }
    }
}
