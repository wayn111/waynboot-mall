package com.wayn.domain.api.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wayn.domain.api.trade.entity.Order;

import java.util.List;

/**
 * 订单表 Mapper 接口
 *
 * @author wayn
 * @since 2020-08-11
 */
public interface OrderMapper extends BaseMapper<Order> {

    IPage<Order> selectOrderListPage(IPage<Order> page, Order order, List<Short> orderStatusList);

    /**
     * 批量插入订单主表。
     * 用于 MQ 批量下单消费链路，依赖 MyBatis generated keys 回填每个订单 ID，供订单商品明细批量写入使用。
     *
     * @param orderList 待插入订单列表
     * @return 插入行数
     */
    int insertBatch(List<Order> orderList);
}
