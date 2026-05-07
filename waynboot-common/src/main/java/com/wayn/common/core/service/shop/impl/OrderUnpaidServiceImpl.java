package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.mapper.shop.OrderMapper;
import com.wayn.common.core.service.shop.IOrderUnpaidService;
import com.wayn.common.core.service.shop.support.order.OrderCancellationSupport;
import com.wayn.util.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 未支付订单超时关闭入口。
 * 实际的取消、回库和回券逻辑统一收敛到订单取消支撑服务中执行。
 */
@Slf4j
@Service
@AllArgsConstructor
public class OrderUnpaidServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderUnpaidService {

    private final OrderCancellationSupport orderCancellationSupport;

    /**
     * 委托执行未支付超时关单。
     *
     * @param orderSn 订单号
     * @param statusAutoCancel 自动关闭状态
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unpaid(String orderSn, OrderStatusEnum statusAutoCancel) {
        log.info("订单编号：{}，未支付取消操作开始", orderSn);
        orderCancellationSupport.cancel(orderSn, statusAutoCancel);
        log.info("订单编号：{}，未支付取消操作结束", orderSn);
    }
}
