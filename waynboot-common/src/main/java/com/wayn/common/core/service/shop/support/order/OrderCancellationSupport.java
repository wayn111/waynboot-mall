package com.wayn.common.core.service.shop.support.order;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.ShopMemberCoupon;
import com.wayn.common.core.mapper.shop.OrderMapper;
import com.wayn.common.core.service.shop.ShopMemberCouponService;
import com.wayn.common.core.service.shop.support.common.TradeLockSupport;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.util.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 订单取消补偿支撑服务。
 * 负责在同一个事务内完成订单状态关闭、冻结库存释放和优惠券回退，并通过分布式锁避免重复取消。
 */
@Slf4j
@Service
@AllArgsConstructor
public class OrderCancellationSupport {

    private final OrderMapper orderMapper;
    private final OrderStockSupport orderStockSupport;
    private final ShopMemberCouponService shopMemberCouponService;
    private final TradeLockSupport tradeLockSupport;
    private final OrderStateTransitionSupport orderStateTransitionSupport;

    /**
     * 取消订单并执行补偿动作。
     *
     * @param orderSn 订单号
     * @param targetStatus 目标关闭状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(String orderSn, OrderStatusEnum targetStatus) {
        String lockKey = RedisKeyEnum.ORDER_UNPAID_KEY.getKey(orderSn);
        boolean executed = tradeLockSupport.tryRunWithLock(lockKey, null, () -> {
            doCancel(orderSn, targetStatus);
        });
        if (!executed) {
            log.warn("订单编号：{} 获取取消锁失败", orderSn);
        }
    }

    /**
     * 在锁内真正执行订单取消。
     *
     * @param orderSn 订单号
     * @param targetStatus 目标关闭状态
     */
    private void doCancel(String orderSn, OrderStatusEnum targetStatus) {
        Order order = orderMapper.selectOne(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderSn, orderSn));
        if (order == null || !orderStateTransitionSupport.canTransition(order.getOrderStatus(), targetStatus)) {
            return;
        }

        // 仅允许从“待支付”原子切换到目标关闭状态，避免支付成功和超时关闭并发覆盖。
        int updated = orderMapper.update(null, Wrappers.lambdaUpdate(Order.class)
                .set(Order::getOrderStatus, targetStatus.getStatus())
                .set(Order::getOrderEndTime, LocalDateTime.now())
                .set(Order::getUpdateTime, new Date())
                .eq(Order::getId, order.getId())
                .eq(Order::getOrderStatus, OrderStatusEnum.STATUS_CREATE.getStatus()));
        if (updated == 0) {
            return;
        }

        // 只有状态更新成功后才释放冻结库存，避免支付成功和超时关闭并发时重复回补可售库存。
        orderStockSupport.releaseFrozenStockByOrderId(order.getId());
        shopMemberCouponService.lambdaUpdate()
                .set(ShopMemberCoupon::getUseStatus, 0)
                .set(ShopMemberCoupon::getUpdateTime, new Date())
                .eq(ShopMemberCoupon::getOrderId, order.getId())
                .eq(ShopMemberCoupon::getUseStatus, 1)
                .update();
    }
}
