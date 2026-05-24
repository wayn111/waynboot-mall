package com.wayn.domain.api.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.request.OrderManagerReqVO;
import com.wayn.domain.api.trade.response.OrderManagerResVO;

/**
 * 类目表 Mapper 接口
 *
 * @author wayn
 * @since 2020-06-26
 */
public interface AdminOrderMapper extends BaseMapper<Order> {

    IPage<OrderManagerResVO> selectOrderListPage(IPage<Order> page, OrderManagerReqVO order);
}
