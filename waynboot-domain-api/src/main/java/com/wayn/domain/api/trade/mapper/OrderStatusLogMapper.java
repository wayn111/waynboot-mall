package com.wayn.domain.api.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.domain.api.trade.entity.OrderStatusLog;

/**
 * 订单状态流转日志 Mapper。
 * 只负责状态日志基础持久化，状态变更时机由各订单编排支撑类控制。
 */
public interface OrderStatusLogMapper extends BaseMapper<OrderStatusLog> {
}
