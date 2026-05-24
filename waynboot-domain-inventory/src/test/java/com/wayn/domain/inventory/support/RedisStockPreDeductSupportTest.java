package com.wayn.domain.inventory.support;

import com.wayn.domain.api.cart.entity.Cart;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.util.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisStockPreDeductSupportTest {

    @Mock
    private RedisCache redisCache;

    @Test
    void preDeductAggregatesSameProductAndReleasesReservation() {
        when(redisCache.luaReserveStock("trade:stock:available:100", "trade:stock:reserved:100",
                "trade:stock:order:order-1:100", 5, 30)).thenReturn(1L);

        RedisStockReservation reservation = newSupport().preDeduct("order-1", List.of(
                buildCart(10L, 100L, 2),
                buildCart(10L, 100L, 3)));
        reservation.release();

        verify(redisCache).luaReserveStock("trade:stock:available:100", "trade:stock:reserved:100",
                "trade:stock:order:order-1:100", 5, 30);
        verify(redisCache).luaReleaseReservedStock("trade:stock:reserved:100",
                "trade:stock:order:order-1:100");
    }

    @Test
    void preDeductThrowsWhenRedisStockInsufficient() {
        when(redisCache.luaReserveStock("trade:stock:available:100", "trade:stock:reserved:100",
                "trade:stock:order:order-2:100", 2, 30)).thenReturn(-1L);

        assertThrows(BusinessException.class,
                () -> newSupport().preDeduct("order-2", List.of(buildCart(10L, 100L, 2))));
    }

    @Test
    void preDeductReleasesPreviousReservationsWhenLaterProductFails() {
        when(redisCache.luaReserveStock("trade:stock:available:100", "trade:stock:reserved:100",
                "trade:stock:order:order-4:100", 1, 30)).thenReturn(1L);
        when(redisCache.luaReserveStock("trade:stock:available:200", "trade:stock:reserved:200",
                "trade:stock:order:order-4:200", 1, 30)).thenReturn(-1L);

        assertThrows(BusinessException.class, () -> newSupport().preDeduct("order-4", List.of(
                buildCart(10L, 100L, 1),
                buildCart(20L, 200L, 1))));

        verify(redisCache).luaReleaseReservedStock("trade:stock:reserved:100",
                "trade:stock:order:order-4:100");
    }

    @Test
    void preDeductSkipsWhenRedisStockNotInitialized() {
        when(redisCache.luaReserveStock("trade:stock:available:100", "trade:stock:reserved:100",
                "trade:stock:order:order-3:100", 2, 30)).thenReturn(-2L);

        RedisStockReservation reservation = newSupport().preDeduct("order-3", List.of(buildCart(10L, 100L, 2)));
        reservation.release();

        verify(redisCache).luaReserveStock("trade:stock:available:100", "trade:stock:reserved:100",
                "trade:stock:order:order-3:100", 2, 30);
    }

    @Test
    void preDeductUsesBucketKeyWhenHotSkuBucketCountExists() {
        when(redisCache.getCacheObject("trade:stock:bucket:count:100")).thenReturn(4);
        when(redisCache.luaReserveStock("trade:stock:available:100:bucket:1", "trade:stock:reserved:100:bucket:1",
                "trade:stock:order:order-5:100:bucket:1", 2, 30)).thenReturn(1L);

        RedisStockReservation reservation = newSupport().preDeduct("order-5", List.of(buildCart(10L, 100L, 2)));
        reservation.release();

        verify(redisCache).luaReserveStock("trade:stock:available:100:bucket:1", "trade:stock:reserved:100:bucket:1",
                "trade:stock:order:order-5:100:bucket:1", 2, 30);
        verify(redisCache).luaReleaseReservedStock("trade:stock:reserved:100:bucket:1",
                "trade:stock:order:order-5:100:bucket:1");
    }

    @Test
    void preDeductSkipsEmptyCheckedGoodsList() {
        RedisStockReservation reservation = newSupport().preDeduct("order-empty", List.of());

        assertDoesNotThrow(reservation::release);
        verifyNoInteractions(redisCache);
    }

    @Test
    void preDeductRejectsInvalidCheckedGoodsBeforeCallingRedis() {
        assertThrows(BusinessException.class,
                () -> newSupport().preDeduct("order-invalid", List.of(buildCart(10L, null, 1))));

        verify(redisCache, org.mockito.Mockito.never()).luaReserveStock(any(), any(), any(), any(), any());
    }

    /**
     * 创建 Redis 库存预扣服务。
     * 所有用例共用同一组 Mock，避免重复构造掩盖依赖变更。
     *
     * @return Redis 库存预扣服务
     */
    private RedisStockPreDeductSupport newSupport() {
        return new RedisStockPreDeductSupport(redisCache);
    }

    /**
     * 构建购物车测试对象。
     *
     * @param goodsId 商品 ID
     * @param productId 商品货品 ID
     * @param number 购买数量
     * @return 购物车对象
     */
    private Cart buildCart(Long goodsId, Long productId, Integer number) {
        Cart cart = new Cart();
        cart.setGoodsId(goodsId);
        cart.setProductId(productId);
        cart.setNumber(number);
        return cart;
    }
}
