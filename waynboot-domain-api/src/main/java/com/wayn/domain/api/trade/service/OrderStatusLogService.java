package com.wayn.domain.api.trade.service;

/**
 * 订单状态日志服务接口。
 * 用于记录订单状态机流转审计日志，具体持久化实现由交易领域模块提供。
 */
public interface OrderStatusLogService {

    /**
     * 记录成功状态流转。
     *
     * @param command 状态流转命令
     */
    void recordSuccess(OrderStatusChangeCommand command);

    /**
     * 记录失败状态流转尝试。
     *
     * @param command 状态流转命令
     * @param failReason 失败原因
     */
    void recordFailure(OrderStatusChangeCommand command, String failReason);
}
