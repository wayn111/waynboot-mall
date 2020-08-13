package com.wayn.mobile.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.Order;
import com.wayn.mobile.api.domain.vo.OrderVO;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-08-11
 */
public interface IOrderService extends IService<Order> {

    /**
     * 添加订单记录
     *
     * @param orderVO 订单VO
     * @return R
     */
    R addOrder(OrderVO orderVO);

    R h5pay(Long orderId, HttpServletRequest request);

    R prepay(Long orderId, HttpServletRequest request);

}
