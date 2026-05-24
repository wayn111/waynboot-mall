package com.wayn.domain.trade.support.order;

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.promotion.entity.ShopMemberCoupon;
import com.wayn.domain.api.trade.mapper.OrderMapper;
import com.wayn.domain.api.trade.service.OrderStatusLogService;
import com.wayn.domain.api.promotion.service.ShopMemberCouponService;
import com.wayn.domain.api.common.MybatisPlusTableInfoTestHelper;
import com.wayn.domain.api.common.TradeLockSupport;
import com.wayn.domain.inventory.support.OrderStockSupport;
import com.wayn.util.enums.OrderStatusEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCancellationSupportTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(Order.class);
        MybatisPlusTableInfoTestHelper.init(ShopMemberCoupon.class);
    }

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderStockSupport orderStockSupport;
    @Mock
    private ShopMemberCouponService shopMemberCouponService;
    @Mock
    private TradeLockSupport tradeLockSupport;
    @Mock
    private OrderStatusLogService orderStatusLogService;
    @Mock
    private TransactionTemplate transactionTemplate;
    private OrderCancellationSupport support;

    /**
     * 初始化订单取消支撑服务。
     * 测试直接注入真实状态流转组件，确保状态判断和条件更新分支按生产逻辑执行。
     */
    @BeforeEach
    void setUp() {
        support = new OrderCancellationSupport(orderMapper, orderStockSupport, shopMemberCouponService,
                tradeLockSupport, new OrderStateTransitionSupport(), orderStatusLogService, transactionTemplate);
    }

    /**
     * 条件更新失败时不释放库存、不回退优惠券。
     * 该场景模拟支付成功与取消并发时，取消侧 CAS 未命中。
     */
    @Test
    void cancelSkipsCompensationWhenConditionalUpdateAffectsNoRows() {
        Order order = newOrder(1L, "order-cancel-1", OrderStatusEnum.STATUS_CREATE);

        mockLockExecution();
        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.update(any(), any())).thenReturn(0);

        support.cancel("order-cancel-1", OrderStatusEnum.STATUS_CANCEL);

        verify(orderStockSupport, never()).releaseFrozenStockByOrderId(anyLong());
        verify(shopMemberCouponService, never()).lambdaUpdate();
    }

    /**
     * 订单已支付时直接跳过取消逻辑。
     * 状态机拒绝非法流转后，不应触发订单更新和补偿动作。
     */
    @Test
    void cancelSkipsCompensationWhenOrderIsAlreadyProcessed() {
        Order order = newOrder(2L, "order-cancel-2", OrderStatusEnum.STATUS_PAY);

        mockLockExecution();
        when(orderMapper.selectOne(any())).thenReturn(order);

        support.cancel("order-cancel-2", OrderStatusEnum.STATUS_AUTO_CANCEL);

        verify(orderMapper, never()).update(any(), any());
        verify(orderStockSupport, never()).releaseFrozenStockByOrderId(anyLong());
        verify(shopMemberCouponService, never()).lambdaUpdate();
    }

    /**
     * 条件更新成功时记录状态日志并释放订单占用资源。
     * 覆盖正常取消路径，防止后续重构遗漏库存释放或优惠券回退。
     */
    @Test
    void cancelRecordsStatusLogAndReleasesStockWhenConditionalUpdateSucceeds() {
        Order order = newOrder(3L, "order-cancel-3", OrderStatusEnum.STATUS_CREATE);

        mockLockExecution();
        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.update(any(), any())).thenReturn(1);
        mockCouponRollbackUpdate();

        support.cancel("order-cancel-3", OrderStatusEnum.STATUS_CANCEL);

        verify(orderStatusLogService).recordSuccess(any());
        verify(orderStockSupport).releaseFrozenStockByOrderId(3L);
        verify(shopMemberCouponService).lambdaUpdate();
    }

    /**
     * 创建订单测试数据。
     *
     * @param orderId 订单 ID
     * @param orderSn 订单号
     * @param status 订单状态
     * @return 订单实体
     */
    private Order newOrder(Long orderId, String orderSn, OrderStatusEnum status) {
        Order order = new Order();
        order.setId(orderId);
        order.setOrderSn(orderSn);
        order.setOrderStatus(status.getStatus());
        return order;
    }

    /**
     * 模拟成功获取订单取消锁并立即执行锁内逻辑。
     * 这里不验证锁实现本身，只让测试进入订单取消的业务分支。
     */
    private void mockLockExecution() {
        when(tradeLockSupport.tryRunWithLock(anyString(), isNull(), any()))
                .thenAnswer(invocation -> {
                    invocation.<Runnable>getArgument(2).run();
                    return true;
                });
        org.mockito.Mockito.doAnswer(invocation -> {
            invocation.<Consumer<TransactionStatus>>getArgument(0).accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    /**
     * 模拟 MyBatis-Plus 链式更新。
     * 成功取消订单会进入优惠券回退分支，测试只关心分支被执行，链式 API 本身不连接数据库。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void mockCouponRollbackUpdate() {
        LambdaUpdateChainWrapper couponUpdateWrapper = mock(LambdaUpdateChainWrapper.class);
        when(shopMemberCouponService.lambdaUpdate()).thenReturn(couponUpdateWrapper);
        when(couponUpdateWrapper.set(any(), any())).thenReturn(couponUpdateWrapper);
        when(couponUpdateWrapper.eq(any(), any())).thenReturn(couponUpdateWrapper);
        when(couponUpdateWrapper.update()).thenReturn(true);
    }
}
