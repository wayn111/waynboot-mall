package com.wayn.mobile.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.util.R;

/**
 * 订单表 服务类
 *
 * @author wayn
 * @since 2020-08-11
 */
public interface IOrderUnpaidService extends IService<Order> {
    R unpaid(String orderSn);
}
