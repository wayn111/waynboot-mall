package com.wayn.domain.trade.support.order.submit.chain;

/**
 * 下单责任链执行模式。
 * 当前只保留单笔完整提交模式，避免批量落单和异步确认链路继续增加订单状态复杂度。
 */
public enum OrderSubmitMode {

    /**
     * 单笔订单完整提交模式。
     */
    SINGLE
}
