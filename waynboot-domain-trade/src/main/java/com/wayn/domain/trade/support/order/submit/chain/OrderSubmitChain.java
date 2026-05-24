package com.wayn.domain.trade.support.order.submit.chain;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 下单责任链执行器。
 * 统一串联订单查重、上下文构建、库存扣减、订单组装、单笔落库和后置消息步骤，外层只选择执行模式。
 */
@Component
public class OrderSubmitChain {

    private final List<OrderSubmitStep> steps;

    /**
     * 构造下单责任链执行器。
     *
     * @param steps Spring 注入的责任链步骤列表
     */
    public OrderSubmitChain(List<OrderSubmitStep> steps) {
        // Spring 注入的 List 顺序不应成为业务顺序来源，统一按步骤 order 显式排序。
        this.steps = steps.stream()
                .sorted(Comparator.comparingInt(OrderSubmitStep::order))
                .toList();
    }

    /**
     * 执行下单责任链。
     * 某个步骤中断上下文后会跳过后续步骤，避免重复订单继续扣库存或落库。
     *
     * @param context 下单责任链上下文
     * @return 执行后的下单责任链上下文
     */
    public OrderSubmitChainContext execute(OrderSubmitChainContext context) {
        for (OrderSubmitStep step : steps) {
            if (context.isStopped()) {
                // 查重等前置步骤会主动中断责任链，防止后续库存和落库步骤产生重复副作用。
                break;
            }
            if (step.supports(context)) {
                // 每个步骤只负责一个副作用点，执行顺序由步骤 order 明确控制。
                step.execute(context);
            }
        }
        return context;
    }
}
