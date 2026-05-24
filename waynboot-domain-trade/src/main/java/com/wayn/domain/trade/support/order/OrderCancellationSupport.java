package com.wayn.domain.trade.support.order;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.promotion.entity.ShopMemberCoupon;
import com.wayn.domain.api.trade.enums.OrderStatusChangeTypeEnum;
import com.wayn.domain.api.trade.mapper.OrderMapper;
import com.wayn.domain.api.trade.service.OrderStatusChangeCommand;
import com.wayn.domain.api.trade.service.OrderStatusLogService;
import com.wayn.domain.api.promotion.service.ShopMemberCouponService;
import com.wayn.domain.api.common.TradeLockSupport;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Date;
import com.wayn.domain.inventory.support.OrderStockSupport;
import com.wayn.domain.inventory.support.RedisStockPreDeductSupport;
import com.wayn.domain.inventory.support.RedisStockSnapshotSupport;
import com.wayn.domain.inventory.support.RedisStockBucketRouter;
import com.wayn.domain.inventory.support.RedisStockKeySupport;
import com.wayn.domain.inventory.support.RedisStockReservation;

/**
 * 订单取消补偿支撑服务。
 * 负责在分布式锁内完成订单状态关闭、冻结库存释放和优惠券回退；事务通过 TransactionTemplate 在锁内提交，
 * 保证锁释放时数据已落库，避免并发请求读取未提交状态。
 */
@Slf4j
@Service
public class OrderCancellationSupport {

    private final OrderMapper orderMapper;
    private final OrderStockSupport orderStockSupport;
    private final ShopMemberCouponService shopMemberCouponService;
    private final TradeLockSupport tradeLockSupport;
    private final OrderStateTransitionSupport orderStateTransitionSupport;
    private final OrderStatusLogService orderStatusLogService;
    private final TransactionTemplate transactionTemplate;

    /**
     * 生产构造器：从事务管理器构造 TransactionTemplate。
     */
    @Autowired
    public OrderCancellationSupport(OrderMapper orderMapper, OrderStockSupport orderStockSupport,
                                    ShopMemberCouponService shopMemberCouponService,
                                    TradeLockSupport tradeLockSupport,
                                    OrderStateTransitionSupport orderStateTransitionSupport,
                                    OrderStatusLogService orderStatusLogService,
                                    PlatformTransactionManager platformTransactionManager) {
        this(orderMapper, orderStockSupport, shopMemberCouponService, tradeLockSupport,
                orderStateTransitionSupport, orderStatusLogService,
                new TransactionTemplate(platformTransactionManager));
    }

    /**
     * 单元测试构造器：可直接注入 TransactionTemplate 桩。
     */
    public OrderCancellationSupport(OrderMapper orderMapper, OrderStockSupport orderStockSupport,
                                    ShopMemberCouponService shopMemberCouponService,
                                    TradeLockSupport tradeLockSupport,
                                    OrderStateTransitionSupport orderStateTransitionSupport,
                                    OrderStatusLogService orderStatusLogService,
                                    TransactionTemplate transactionTemplate) {
        this.orderMapper = orderMapper;
        this.orderStockSupport = orderStockSupport;
        this.shopMemberCouponService = shopMemberCouponService;
        this.tradeLockSupport = tradeLockSupport;
        this.orderStateTransitionSupport = orderStateTransitionSupport;
        this.orderStatusLogService = orderStatusLogService;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * 取消订单并执行补偿动作。
     * 事务在锁内通过 TransactionTemplate 提交，确保锁释放时数据已落库，避免并发请求读取未提交状态。
     *
     * @param orderSn 订单号
     * @param targetStatus 目标关闭状态
     */
    public void cancel(String orderSn, OrderStatusEnum targetStatus) {
        String lockKey = RedisKeyEnum.ORDER_UNPAID_KEY.getKey(orderSn);
        boolean executed = tradeLockSupport.tryRunWithLock(lockKey, null,
                () -> transactionTemplate.executeWithoutResult(status -> doCancel(orderSn, targetStatus)));
        if (!executed) {
            log.warn("订单编号：{} 获取取消锁失败", orderSn);
            throw new BusinessException(ReturnCodeEnum.ERROR);
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
        OrderStatusEnum sourceStatus = orderStateTransitionSupport.resolve(order.getOrderStatus());
        OrderStatusChangeCommand command = buildCancelStatusLogCommand(order, sourceStatus, targetStatus);

        int updated = closeOrder(order, targetStatus);
        if (updated == 0) {
            orderStatusLogService.recordFailure(command, "订单状态条件更新失败");
            return;
        }

        orderStatusLogService.recordSuccess(command);
        compensateCancelledOrder(order);
    }

    /**
     * 关闭订单状态。
     * 仅允许从“待支付”原子切换到目标关闭状态，避免支付成功和超时关闭并发覆盖。
     *
     * @param order 订单
     * @param targetStatus 目标关闭状态
     * @return 影响行数
     */
    private int closeOrder(Order order, OrderStatusEnum targetStatus) {
        var updateWrapper = Wrappers.lambdaUpdate(Order.class)
                .set(Order::getOrderStatus, targetStatus.getStatus())
                .set(Order::getOrderEndTime, LocalDateTime.now())
                .set(Order::getUpdateTime, new Date())
                .eq(Order::getId, order.getId());
        orderStateTransitionSupport.applyExpectedStatus(updateWrapper, OrderStatusEnum.STATUS_CREATE);
        return orderMapper.update(null, updateWrapper);
    }

    /**
     * 执行取消订单后的补偿动作。
     * 只有订单状态条件更新成功后才释放冻结库存和回退优惠券，避免支付成功与超时关闭并发时重复回补资源。
     *
     * @param order 订单
     */
    private void compensateCancelledOrder(Order order) {
        orderStockSupport.releaseFrozenStockByOrderId(order.getId());
        rollbackUsedCoupons(order.getId());
    }

    /**
     * 回退已绑定到订单的优惠券。
     * 条件中保留已使用状态，避免重复取消或补偿重试时把非本次订单占用的优惠券误改回可用。
     *
     * @param orderId 订单 ID
     */
    private void rollbackUsedCoupons(Long orderId) {
        shopMemberCouponService.lambdaUpdate()
                .set(ShopMemberCoupon::getUseStatus, 0)
                .set(ShopMemberCoupon::getUpdateTime, new Date())
                .eq(ShopMemberCoupon::getOrderId, orderId)
                .eq(ShopMemberCoupon::getUseStatus, 1)
                .update();
    }

    /**
     * 构建取消订单状态日志命令。
     *
     * @param order 订单
     * @param sourceStatus 来源状态
     * @param targetStatus 目标关闭状态
     * @return 状态日志命令
     */
    private OrderStatusChangeCommand buildCancelStatusLogCommand(Order order, OrderStatusEnum sourceStatus,
                                                                 OrderStatusEnum targetStatus) {
        boolean autoCancel = targetStatus == OrderStatusEnum.STATUS_AUTO_CANCEL;
        return OrderStatusChangeCommand.builder()
                .orderId(order.getId())
                .orderSn(order.getOrderSn())
                .sourceStatus(sourceStatus)
                .targetStatus(targetStatus)
                .changeType(autoCancel ? OrderStatusChangeTypeEnum.AUTO_CANCEL : OrderStatusChangeTypeEnum.USER_CANCEL)
                .operatorType(autoCancel ? "SYSTEM" : "USER")
                .operatorId(autoCancel ? "system" : String.valueOf(order.getUserId()))
                .remark(autoCancel ? "订单超时自动取消" : "用户取消订单")
                .build();
    }
}
