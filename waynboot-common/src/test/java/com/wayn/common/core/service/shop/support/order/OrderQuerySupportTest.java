package com.wayn.common.core.service.shop.support.order;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.mapper.shop.OrderMapper;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.core.service.shop.support.common.MybatisPlusTableInfoTestHelper;
import com.wayn.common.model.response.OrderListDataResVO;
import com.wayn.common.model.response.OrderListResVO;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.util.enums.OrderStatusEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderQuerySupportTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(Order.class, OrderGoods.class);
    }

    /**
     * 验证订单列表查询会批量加载订单商品并组装分页返回。
     */
    @Test
    void selectListPageBuildsOrderListWithGoodsSnapshots() {
        RedisCache redisCache = mock(RedisCache.class);
        OrderMapper orderMapper = mock(OrderMapper.class);
        IOrderGoodsService orderGoodsService = mock(IOrderGoodsService.class);
        OrderQuerySupport support = new OrderQuerySupport(redisCache, orderMapper, orderGoodsService);
        Page<Order> page = new Page<>(1, 10);
        Page<Order> orderPage = new Page<>(1, 10);
        orderPage.setRecords(List.of(buildOrder()));
        orderPage.setTotal(21);
        OrderGoods orderGoods = buildOrderGoods();
        when(orderMapper.selectOrderListPage(any(IPage.class), any(Order.class), any()))
                .thenReturn(orderPage);
        when(orderGoodsService.list(org.mockito.ArgumentMatchers.<Wrapper<OrderGoods>>any()))
                .thenReturn(List.of(orderGoods));

        OrderListResVO resVO = support.selectListPage(page, 0, 9L);

        assertEquals(1, resVO.getPage());
        assertEquals(3, resVO.getPages());
        assertEquals(1, resVO.getData().size());
        OrderListDataResVO data = resVO.getData().get(0);
        assertEquals(1L, data.getId());
        assertEquals("ORDER-1", data.getOrderSn());
        assertEquals(new BigDecimal("20.00"), data.getActualPrice());
        assertEquals(1, data.getGoodsList().size());
        assertEquals(100L, data.getGoodsList().get(0).getGoodsId());
        verify(orderGoodsService).list(org.mockito.ArgumentMatchers.<Wrapper<OrderGoods>>any());
    }

    /**
     * 构建订单主表测试数据。
     *
     * @return 订单主表
     */
    private Order buildOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(9L);
        order.setOrderSn("ORDER-1");
        order.setOrderStatus(OrderStatusEnum.STATUS_CREATE.getStatus());
        order.setActualPrice(new BigDecimal("20.00"));
        return order;
    }

    /**
     * 构建订单商品测试数据。
     *
     * @return 订单商品快照
     */
    private OrderGoods buildOrderGoods() {
        OrderGoods orderGoods = new OrderGoods();
        orderGoods.setOrderId(1L);
        orderGoods.setGoodsId(100L);
        orderGoods.setProductId(1000L);
        orderGoods.setNumber(2);
        return orderGoods;
    }
}
