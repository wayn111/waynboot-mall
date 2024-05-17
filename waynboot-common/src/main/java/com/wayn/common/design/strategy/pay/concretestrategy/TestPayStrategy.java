package com.wayn.common.design.strategy.pay.concretestrategy;

import cn.hutool.core.lang.id.NanoId;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.service.shop.IOrderService;
import com.wayn.common.design.strategy.pay.PayTypeEnum;
import com.wayn.common.design.strategy.pay.strategy.PayTypeInterface;
import com.wayn.common.request.OrderPayReqVO;
import com.wayn.common.response.OrderPayResVO;
import com.wayn.util.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 微信JSAPI支付策略
 */
@Slf4j
@Component
@AllArgsConstructor
public class TestPayStrategy implements PayTypeInterface {
    private IOrderService orderService;

    @Override
    public OrderPayResVO pay(OrderPayReqVO reqVo) {
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_sn", reqVo.getOrderSn()));
        order.setPayId(NanoId.randomNanoId());
        order.setPayTime(LocalDateTime.now());
        order.setOrderStatus(OrderStatusEnum.STATUS_PAY.getStatus());
        order.setUpdateTime(new Date());
        orderService.updateById(order);
        return new OrderPayResVO();
    }

    @Override
    public Integer getType() {
        return PayTypeEnum.TEST.getType();
    }
}
