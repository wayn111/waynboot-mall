package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.shop.OrderGoods;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.core.util.OrderUtil;
import com.wayn.common.util.R;
import com.wayn.mobile.api.mapper.OrderMapper;
import com.wayn.mobile.api.service.IMobileOrderService;
import com.wayn.mobile.api.service.IOrderUnpaidService;
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
    public R unpaid(String orderSn) {
        log.info("订单编号：{}，未支付取消操作开始", orderSn);
        Order order = orderService.getOne(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderSn, orderSn));
        // 订单状态不是刚生成不做处理
        if (!OrderUtil.isCreateStatus(order)) {
            return R.success();
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
        return R.success();
    }

}
