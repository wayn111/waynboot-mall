package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.entity.shop.Order;

/**
 * 订单表 服务类
 *
 * @author wayn
 * @since 2020-08-11
 */
public interface IOrderUnpaidService extends IService<Order> {
    void unpaid(String orderSn);
}
