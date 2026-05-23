package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.common.core.entity.shop.OrderStatusLog;

/**
 * 订单状态流转日志 Mapper。
 * 只负责状态日志基础持久化，状态变更时机由各订单编排支撑类控制。
 */
public interface OrderStatusLogMapper extends BaseMapper<OrderStatusLog> {
}
