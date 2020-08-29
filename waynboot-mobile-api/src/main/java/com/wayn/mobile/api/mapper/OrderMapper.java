package com.wayn.mobile.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wayn.common.core.domain.shop.Order;

import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author wayn
 * @since 2020-08-11
 */
public interface OrderMapper extends BaseMapper<Order> {

    IPage<Order> selectOrderListPage(IPage<Order> page, Order order, List<Short> orderStatusList);
}
