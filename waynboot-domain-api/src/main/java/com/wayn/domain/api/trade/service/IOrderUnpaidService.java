package com.wayn.domain.api.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.util.enums.OrderStatusEnum;

/**
 * 订单表 服务类
 *
 * @author wayn
 * @since 2020-08-11
 */
public interface IOrderUnpaidService extends IService<Order> {
    void unpaid(String orderSn, OrderStatusEnum statusAutoCancel);
}
