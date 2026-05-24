package com.wayn.domain.trade.support.order.submit.chain;

import com.wayn.domain.trade.support.order.OrderSubmitContext;
import com.wayn.message.core.dto.OrderDTO;

/**
 * 下单上下文工厂。
 * 由编排层提供上下文构建能力，责任链步骤只关心何时构建，避免步骤直接依赖购物车、地址、优惠券等入口参数解析细节。
 */
@FunctionalInterface
public interface OrderSubmitContextFactory {

    /**
     * 根据下单 DTO 构建下单上下文。
     *
     * @param orderDTO 下单 DTO
     * @return 下单上下文
     */
    OrderSubmitContext create(OrderDTO orderDTO);
}
