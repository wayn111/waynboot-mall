package com.wayn.domain.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.mapper.OrderMapper;
import com.wayn.domain.api.trade.service.IOrderUnpaidService;
import com.wayn.domain.trade.support.order.OrderCancellationSupport;
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
