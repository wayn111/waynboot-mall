package com.wayn.domain.inventory.support;

import com.wayn.domain.api.cart.entity.Cart;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.trade.entity.OrderGoods;
import com.wayn.domain.api.inventory.service.InventoryFlowService;
import com.wayn.domain.api.goods.service.IGoodsProductService;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.domain.api.trade.service.IOrderGoodsService;
import com.wayn.data.redis.manager.RedisCache;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.wayn.data.redis.constant.RedisKeyEnum.GOODS_DETAIL_CACHE;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderStockSupportTest {

    @Mock
    private IGoodsProductService goodsProductService;
    @Mock
    private IGoodsService goodsService;
    @Mock
    private IOrderGoodsService orderGoodsService;
    @Mock
    private RedisCache redisCache;
    @Mock
    private InventoryFlowService inventoryFlowService;

    @Test
    void freezeStockAggregatesSameProductBeforeDeducting() {
        OrderStockSupport support = new OrderStockSupport(goodsProductService, goodsService, orderGoodsService,
                inventoryFlowService, redisCache);
        Cart firstCart = buildCart(10L, 100L, 2);
        Cart secondCart = buildCart(10L, 100L, 3);
        GoodsProduct product = new GoodsProduct();
        product.setId(100L);
        product.setNumber(10);
        product.setLockedStock(0);
        product.setSpecifications(new String[]{"红色", "L"});

        when(goodsProductService.selectProductByIds(List.of(100L))).thenReturn(List.of(product));
        when(goodsProductService.freezeStock(100L, 5)).thenReturn(true);
        when(inventoryFlowService.saveFlow(org.mockito.ArgumentMatchers.any())).thenReturn(true);

        support.freezeStock("order-1001", List.of(firstCart, secondCart));

        verify(goodsProductService).selectProductByIds(List.of(100L));
        verify(goodsProductService).freezeStock(100L, 5);
        verify(goodsProductService, never()).freezeStock(100L, 2);
        verify(goodsProductService, never()).freezeStock(100L, 3);
        verify(inventoryFlowService).saveFlow(org.mockito.ArgumentMatchers.any());
        verify(redisCache).deleteObject(GOODS_DETAIL_CACHE.getKey(10L));
    }

    @Test
    void confirmFrozenStockByOrderIdUsesFlowIdempotencyBeforeConfirming() {
        OrderStockSupport support = new OrderStockSupport(goodsProductService, goodsService, orderGoodsService,
                inventoryFlowService, redisCache);
        OrderGoods orderGoods = buildOrderGoods(1L, 10L, 100L, 3);

        when(orderGoodsService.list(org.mockito.ArgumentMatchers.<Wrapper<OrderGoods>>any()))
                .thenReturn(List.of(orderGoods));
        when(inventoryFlowService.saveFlow(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(goodsProductService.confirmFrozenStock(100L, 3)).thenReturn(true);

        support.confirmFrozenStockByOrderId(1L);

        verify(inventoryFlowService).saveFlow(org.mockito.ArgumentMatchers.any());
        verify(goodsProductService).confirmFrozenStock(100L, 3);
        verify(redisCache).deleteObject(GOODS_DETAIL_CACHE.getKey(10L));
    }

    @Test
    void confirmFrozenStockByOrderIdSkipsConfirmWhenFlowAlreadyExists() {
        OrderStockSupport support = new OrderStockSupport(goodsProductService, goodsService, orderGoodsService,
                inventoryFlowService, redisCache);
        OrderGoods orderGoods = buildOrderGoods(1L, 10L, 100L, 3);

        when(orderGoodsService.list(org.mockito.ArgumentMatchers.<Wrapper<OrderGoods>>any()))
                .thenReturn(List.of(orderGoods));
        when(inventoryFlowService.saveFlow(org.mockito.ArgumentMatchers.any())).thenReturn(false);

        support.confirmFrozenStockByOrderId(1L);

        verify(goodsProductService, never()).confirmFrozenStock(100L, 3);
    }

    @Test
    void releaseFrozenStockByOrderIdReturnsLockedStockToAvailable() {
        OrderStockSupport support = new OrderStockSupport(goodsProductService, goodsService, orderGoodsService,
                inventoryFlowService, redisCache);
        OrderGoods orderGoods = buildOrderGoods(1L, 10L, 100L, 3);

        when(orderGoodsService.list(org.mockito.ArgumentMatchers.<Wrapper<OrderGoods>>any()))
                .thenReturn(List.of(orderGoods));
        when(inventoryFlowService.saveFlow(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(goodsProductService.releaseFrozenStock(100L, 3)).thenReturn(true);

        support.releaseFrozenStockByOrderId(1L);

        verify(inventoryFlowService).saveFlow(org.mockito.ArgumentMatchers.any());
        verify(goodsProductService).releaseFrozenStock(100L, 3);
        verify(redisCache).deleteObject(GOODS_DETAIL_CACHE.getKey(10L));
    }

    private Cart buildCart(Long goodsId, Long productId, Integer number) {
        Cart cart = new Cart();
        cart.setGoodsId(goodsId);
        cart.setProductId(productId);
        cart.setNumber(number);
        return cart;
    }

    private OrderGoods buildOrderGoods(Long orderId, Long goodsId, Long productId, Integer number) {
        OrderGoods orderGoods = new OrderGoods();
        orderGoods.setOrderId(orderId);
        orderGoods.setGoodsId(goodsId);
        orderGoods.setProductId(productId);
        orderGoods.setNumber(number);
        return orderGoods;
    }
}
