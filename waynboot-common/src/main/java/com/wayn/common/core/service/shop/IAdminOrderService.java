package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Banner;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.util.R;

/**
 * <p>
 * 类目表 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-06-26
 */
public interface IAdminOrderService extends IService<Order> {

    /**
     * 获取订单列表
     *
     * @param page     分页对象
     * @param order 订单对象
     * @return r
     */
    IPage<Order> selectListPage(IPage<Order> page, Order order);

}
