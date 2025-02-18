package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.entity.shop.ShopMemberCoupon;
import com.wayn.common.core.mapper.shop.OrderMapper;
import com.wayn.common.core.service.shop.*;
import com.wayn.common.util.OrderUtil;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisLock;
import com.wayn.util.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author: waynaqua
 * @date: 2023/8/15 0:47
 */
@Slf4j
@Service
@AllArgsConstructor
public class OrderUnpaidServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderUnpaidService {

    private IOrderService orderService;
    private IOrderGoodsService orderGoodsService;
    private IGoodsProductService productService;
    private ShopMemberCouponService shopMemberCouponService;
    private RedisLock redisLock;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unpaid(String orderSn, OrderStatusEnum statusAutoCancel) {
        log.info("订单编号：{}，未支付取消操作开始", orderSn);
        try {
            boolean lock = redisLock.lock(RedisKeyEnum.ORDER_UNPAID_KEY.getKey(orderSn));
            if (lock) {

                Order order = orderService.getOne(Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getOrderSn, orderSn));
                // 订单状态不是刚生成不做处理
                if (!OrderUtil.isCreateStatus(order)) {
                    return;
                }
                Long orderId = order.getId();
                // 设置订单为已取消状态
                order.setOrderStatus(statusAutoCancel.getStatus());
                order.setOrderEndTime(LocalDateTime.now());
                order.setUpdateTime(new Date());
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
                // 修改优惠卷使用状态
                shopMemberCouponService.lambdaUpdate()
                        .set(ShopMemberCoupon::getUseStatus, 0)
                        .eq(ShopMemberCoupon::getOrderId, orderId)
                        .eq(ShopMemberCoupon::getUseStatus, 1)
                        .update();
            }
        } finally {
            redisLock.unLock(RedisKeyEnum.ORDER_UNPAID_KEY.getKey(orderSn));
        }
        log.info("订单编号：{}，未支付取消操作结束", orderSn);
    }

}
