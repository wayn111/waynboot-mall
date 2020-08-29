package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wayn.common.core.domain.shop.Order;

/**
 * <p>
 * 类目表 Mapper 接口
 * </p>
 *
 * @author wayn
 * @since 2020-06-26
 */
public interface AdminOrderMapper extends BaseMapper<Order> {

    IPage<Order> selectOrderListPage(IPage<Order> page, Order order);
}
