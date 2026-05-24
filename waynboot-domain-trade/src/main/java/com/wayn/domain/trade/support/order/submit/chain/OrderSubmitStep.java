package com.wayn.domain.trade.support.order.submit.chain;

/**
 * 下单责任链步骤。
 * 每个步骤只处理一个业务动作，通过顺序号组合出单笔同步落单链路。
 */
public interface OrderSubmitStep {

    /**
     * 订单查重步骤顺序。
     */
    int DUPLICATE_CHECK_ORDER = 100;

    /**
     * 下单上下文构建步骤顺序。
     */
    int CONTEXT_BUILD_ORDER = 200;

    /**
     * 库存扣减步骤顺序。
     */
    int STOCK_REDUCE_ORDER = 300;

    /**
     * 订单对象组装步骤顺序。
     */
    int ORDER_BUILD_ORDER = 400;

    /**
     * 单笔订单持久化步骤顺序。
     */
    int SINGLE_PERSIST_ORDER = 500;

    /**
     * 单笔订单后置消息步骤顺序。
     */
    int SINGLE_DELAY_MESSAGE_ORDER = 600;

    /**
     * 返回步骤顺序。
     *
     * @return 步骤顺序
     */
    int order();

    /**
     * 判断当前步骤是否支持给定执行上下文。
     *
     * @param context 下单责任链上下文
     * @return true=当前步骤需要执行
     */
    default boolean supports(OrderSubmitChainContext context) {
        return true;
    }

    /**
     * 执行责任链步骤。
     *
     * @param context 下单责任链上下文
     */
    void execute(OrderSubmitChainContext context);
}
