package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wayn.common.core.entity.shop.Order;

import java.util.List;

/**
 * 订单表 Mapper 接口
 *
 * @author wayn
 * @since 2020-08-11
 */
public interface OrderMapper extends BaseMapper<Order> {

    IPage<Order> selectOrderListPage(IPage<Order> page, Order order, List<Short> orderStatusList);
}
