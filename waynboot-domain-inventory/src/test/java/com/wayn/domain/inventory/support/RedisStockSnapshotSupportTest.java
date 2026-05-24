package com.wayn.domain.inventory.support;

import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.goods.service.IGoodsProductService;
import com.wayn.data.redis.manager.RedisCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisStockSnapshotSupportTest {

    @Mock
    private IGoodsProductService goodsProductService;

    @Mock
    private RedisCache redisCache;

    @Test
    void refreshProductSnapshotWritesGlobalSnapshotAndBucketKeys() {
        GoodsProduct product = buildProduct(100L, 10);
        when(goodsProductService.getById(100L)).thenReturn(product);

        RedisStockSnapshotRefreshResult result = newSupport().refreshProductSnapshot(100L, 3);

        assertEquals(3, result.getBucketCount());
        assertEquals(10, result.getAvailableStock());
        verify(redisCache).setCacheObject("trade:stock:available:100", 10);
        verify(redisCache).setCacheObject("trade:stock:bucket:count:100", 3);
        verify(redisCache).setCacheObject("trade:stock:available:100:bucket:0", 4);
        verify(redisCache).setCacheObject("trade:stock:available:100:bucket:1", 3);
        verify(redisCache).setCacheObject("trade:stock:available:100:bucket:2", 3);
    }

    @Test
    void refreshProductSnapshotsSkipsEmptyAndNullProductIds() {
        List<RedisStockSnapshotRefreshResult> result = newSupport().refreshProductSnapshots(
                Collections.singletonList(null), 2);

        assertEquals(0, result.size());
        verify(goodsProductService, never()).listByIds(anyCollection());
    }

    @Test
    void refreshProductSnapshotsSkipsProductsWithoutId() {
        when(goodsProductService.listByIds(List.of(100L))).thenReturn(buildProductsWithInvalidItems());

        List<RedisStockSnapshotRefreshResult> result = newSupport().refreshProductSnapshots(List.of(100L), 2);

        assertEquals(0, result.size());
        verify(redisCache, never()).setCacheObject(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    /**
     * 构建包含异常商品货品的列表。
     * 不能使用 List.of 直接放 null；该工厂方法会拒绝 null，导致测试在进入业务代码前失败。
     *
     * @return 包含空 ID 和空对象的商品货品列表
     */
    private List<GoodsProduct> buildProductsWithInvalidItems() {
        List<GoodsProduct> products = new java.util.ArrayList<>();
        products.add(new GoodsProduct());
        products.add(null);
        return products;
    }

    /**
     * 创建 Redis 库存快照服务。
     * 用例复用统一构造逻辑，新增依赖时只需要调整这一处。
     *
     * @return Redis 库存快照服务
     */
    private RedisStockSnapshotSupport newSupport() {
        return new RedisStockSnapshotSupport(goodsProductService, redisCache);
    }

    /**
     * 构建商品货品测试对象。
     *
     * @param productId 商品货品 ID
     * @param number 可售库存
     * @return 商品货品对象
     */
    private GoodsProduct buildProduct(Long productId, Integer number) {
        GoodsProduct product = new GoodsProduct();
        product.setId(productId);
        product.setNumber(number);
        return product;
    }
}
