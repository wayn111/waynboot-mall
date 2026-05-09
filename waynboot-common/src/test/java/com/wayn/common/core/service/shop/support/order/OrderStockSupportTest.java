package com.wayn.common.core.service.shop.support.order;

import com.wayn.common.core.entity.shop.Cart;
import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.data.redis.manager.RedisCache;
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

    @Test
    void reduceStockAggregatesSameProductBeforeDeducting() {
        OrderStockSupport support = new OrderStockSupport(goodsProductService, goodsService, orderGoodsService,
                redisCache);
        Cart firstCart = buildCart(10L, 100L, 2);
        Cart secondCart = buildCart(10L, 100L, 3);
        GoodsProduct product = new GoodsProduct();
        product.setId(100L);
        product.setNumber(10);
        product.setSpecifications(new String[]{"红色", "L"});

        when(goodsProductService.selectProductByIds(List.of(100L))).thenReturn(List.of(product));
        when(goodsProductService.reduceStock(100L, 5)).thenReturn(true);

        support.reduceStock(List.of(firstCart, secondCart));

        verify(goodsProductService).selectProductByIds(List.of(100L));
        verify(goodsProductService).reduceStock(100L, 5);
        verify(goodsProductService, never()).reduceStock(100L, 2);
        verify(goodsProductService, never()).reduceStock(100L, 3);
        verify(redisCache).deleteObject(GOODS_DETAIL_CACHE.getKey(10L));
    }

    private Cart buildCart(Long goodsId, Long productId, Integer number) {
        Cart cart = new Cart();
        cart.setGoodsId(goodsId);
        cart.setProductId(productId);
        cart.setNumber(number);
        return cart;
    }
}
