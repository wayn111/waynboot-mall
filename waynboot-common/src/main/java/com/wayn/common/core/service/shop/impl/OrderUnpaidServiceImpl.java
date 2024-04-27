package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.mapper.shop.OrderMapper;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IMobileOrderService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.core.service.shop.IOrderUnpaidService;
import com.wayn.common.util.OrderUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: waynaqua
 * @date: 2023/8/15 0:47
 */
@Slf4j
@Service
@AllArgsConstructor
public class OrderUnpaidServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderUnpaidService {


    private IMobileOrderService orderService;
    private IOrderGoodsService orderGoodsService;
    private IGoodsProductService productService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unpaid(String orderSn) {
        log.info("订单编号：{}，未支付取消操作开始", orderSn);
        Order order = orderService.getOne(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderSn, orderSn));
        // 订单状态不是刚生成不做处理
        if (!OrderUtil.isCreateStatus(order)) {
            return;
        }
        Long orderId = order.getId();
        // 设置订单为已取消状态
        order.setOrderStatus(OrderUtil.STATUS_AUTO_CANCEL);
        order.setOrderEndTime(LocalDateTime.now());
        if (!orderService.updateById(order)) {
            log.info("订单编号：{} 更新订单状态失败", orderSn);
            throw new RuntimeException("更新订单状态失败");
        }

        // 商品货品数量增加
        List<OrderGoods> orderGoodsList = orderGoodsService.list(Wrappers.lambdaQuery(OrderGoods.class)
                .eq(OrderGoods::getOrderId, orderId));
        for (OrderGoods orderGoods : orderGoodsList) {
            Long productId = orderGoods.getProductId();
            Integer number = orderGoods.getNumber();
            if (!productService.addStock(productId, number)) {
                log.info("订单编号：{} 商品货品库存增加失败", orderId);
                throw new RuntimeException("商品货品库存增加失败");
            }
        }
        log.info("订单编号：{}，未支付取消操作结束", orderSn);
    }

}
