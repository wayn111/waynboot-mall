package com.wayn.common.core.service.shop.support.payment;

import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentPostActionSupportTest {

    /**
     * 验证支付后置动作只提交异步任务，不在回调线程直接查询订单商品。
     */
    @Test
    void handleOrderPaidSubmitsPostActionToExecutor() {
        IOrderGoodsService orderGoodsService = mock(IOrderGoodsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        ThreadPoolTaskExecutor executor = mock(ThreadPoolTaskExecutor.class);
        PaymentPostActionSupport support = new PaymentPostActionSupport(orderGoodsService, goodsService, executor);

        support.handleOrderPaid(1L);

        verify(executor).execute(any(Runnable.class));
        verify(orderGoodsService, never()).list(org.mockito.ArgumentMatchers.<Wrapper<OrderGoods>>any());
    }

    /**
     * 验证异步后置动作会更新订单商品对应的虚拟销量。
     */
    @Test
    void asyncPostActionUpdatesVirtualSales() {
        IOrderGoodsService orderGoodsService = mock(IOrderGoodsService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        DirectThreadPoolTaskExecutor executor = new DirectThreadPoolTaskExecutor();
        PaymentPostActionSupport support = new PaymentPostActionSupport(orderGoodsService, goodsService, executor);
        OrderGoods orderGoods = new OrderGoods();
        orderGoods.setGoodsId(10L);
        orderGoods.setNumber(3);
        when(orderGoodsService.list(org.mockito.ArgumentMatchers.<Wrapper<OrderGoods>>any())).thenReturn(List.of(orderGoods));

        support.handleOrderPaid(1L);

        verify(goodsService).updateVirtualSales(10L, 3);
    }

    /**
     * 测试用同步线程池。
     */
    private static final class DirectThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

        /**
         * 直接执行任务，便于断言异步逻辑结果。
         *
         * @param task 待执行任务
         */
        @Override
        public void execute(Runnable task) {
            task.run();
        }
    }
}
