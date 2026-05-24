package com.wayn.domain.trade.support.order.submit.chain;

import com.wayn.domain.trade.support.order.OrderSubmitMessageSupport;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 单笔订单后置延迟消息步骤。
 * 在单笔完整提交模式注册未支付关单消息，消息会在订单事务提交后发送。
 */
@Component
@AllArgsConstructor
public class OrderSubmitSingleDelayMessageStep implements OrderSubmitStep {

    private final OrderSubmitMessageSupport orderSubmitMessageSupport;

    /**
     * 返回单笔后置消息步骤顺序。
     *
     * @return 步骤顺序
     */
    @Override
    public int order() {
        return SINGLE_DELAY_MESSAGE_ORDER;
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
     * 注册订单未支付超时关单延迟消息。
     *
     * @param context 下单责任链上下文
     */
    @Override
    public void execute(OrderSubmitChainContext context) {
        // 本地消息写入与订单落库处于同一事务，订单回滚时延迟关单消息也会一起回滚。
        orderSubmitMessageSupport.saveUnpaidDelayMessage(context.getOrder().getOrderSn());
    }
}
