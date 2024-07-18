package com.wayn.common.design.strategy.pay.concretestrategy;

import cn.hutool.core.lang.id.NanoId;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
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
import java.util.List;

/**
 * 微信JSAPI支付策略
 */
@Slf4j
@Component
@AllArgsConstructor
public class TestPayStrategy implements PayTypeInterface {
    private IOrderService orderService;
    private IOrderGoodsService iOrderGoodsService;
    private IGoodsService iGoodsService;

    @Override
    public OrderPayResVO pay(OrderPayReqVO reqVo) {
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_sn", reqVo.getOrderSn()));
        order.setPayId(NanoId.randomNanoId());
        order.setPayTime(LocalDateTime.now());
        order.setOrderStatus(OrderStatusEnum.STATUS_PAY.getStatus());
        order.setUpdateTime(new Date());
        orderService.updateById(order);
        updateVirtualSales(order.getId());
        return new OrderPayResVO();
    }

    private void updateVirtualSales(Long orderId) {
        try {
            List<OrderGoods> orderGoods = iOrderGoodsService.list(Wrappers.lambdaQuery(OrderGoods.class)
                    .eq(OrderGoods::getOrderId, orderId));
            for (OrderGoods orderGood : orderGoods) {
                Long goodsId = orderGood.getGoodsId();
                Integer number = orderGood.getNumber();
                iGoodsService.updateVirtualSales(goodsId, number);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Integer getType() {
        return PayTypeEnum.TEST.getType();
    }
}
