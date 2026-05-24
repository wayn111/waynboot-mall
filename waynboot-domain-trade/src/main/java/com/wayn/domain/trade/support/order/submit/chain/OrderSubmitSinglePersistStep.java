package com.wayn.domain.trade.support.order.submit.chain;

import com.wayn.domain.trade.support.order.OrderSubmitPersistenceSupport;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 单笔订单持久化步骤。
 * 在单笔同步落单事务内写入订单主表、订单商品，并处理购物车和优惠券副作用。
 */
@Component
@AllArgsConstructor
public class OrderSubmitSinglePersistStep implements OrderSubmitStep {

    private final OrderSubmitPersistenceSupport orderSubmitPersistenceSupport;

    /**
     * 返回单笔持久化步骤顺序。
     *
     * @return 步骤顺序
     */
    @Override
    public int order() {
        return SINGLE_PERSIST_ORDER;
    }

    /**
     * 判断当前步骤是否仅支持单笔下单模式。
     *
     * @param context 下单责任链上下文
     * @return true=单笔下单模式
     */
    @Override
    public boolean supports(OrderSubmitChainContext context) {
        return context.getMode() == OrderSubmitMode.SINGLE;
    }

    /**
     * 持久化单笔订单及其关联订单商品、购物车、优惠券副作用。
     *
     * @param context 下单责任链上下文
     */
    @Override
    public void execute(OrderSubmitChainContext context) {
        // 持久化动作放在扣库存之后，同一事务内失败会一起回滚库存扣减、购物车清理和优惠券占用。
        orderSubmitPersistenceSupport.persistSingle(context.getOrderDTO(), context.getSubmitContext(), context.getOrder());
    }
}
