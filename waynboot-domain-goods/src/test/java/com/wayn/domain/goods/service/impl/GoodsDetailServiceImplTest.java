package com.wayn.domain.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.entity.GoodsAttribute;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.goods.service.IGoodsAttributeService;
import com.wayn.domain.api.goods.service.IGoodsProductService;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.domain.api.goods.service.IGoodsSpecificationService;
import com.wayn.domain.api.goods.response.SpecificationVO;
import com.wayn.domain.api.goods.response.GoodsDetailResponseVO;
import com.wayn.data.redis.manager.RedisCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static com.wayn.data.redis.constant.RedisKeyEnum.GOODS_DETAIL_CACHE;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsDetailServiceImplTest {

    @Mock
    private IGoodsService goodsService;
    @Mock
    private IGoodsSpecificationService goodsSpecificationService;
    @Mock
    private IGoodsProductService goodsProductService;
    @Mock
    private IGoodsAttributeService goodsAttributeService;
    @Mock
    private RedisCache redisCache;

    /**
     * 验证商品详情缓存命中时不再访问数据库和线程池。
     */
    @Test
    void getGoodsDetailDataReturnsCachedValueWithoutQueryingDatabase() {
        ThreadPoolTaskExecutor executor = mock(ThreadPoolTaskExecutor.class);
        GoodsDetailServiceImpl service = new GoodsDetailServiceImpl(goodsService, goodsSpecificationService,
                goodsProductService, goodsAttributeService, executor, redisCache);
        GoodsDetailResponseVO cached = new GoodsDetailResponseVO();
        when(redisCache.getCacheObject(GOODS_DETAIL_CACHE.getKey(1L))).thenReturn(cached);

        GoodsDetailResponseVO result = service.getGoodsDetailData(1L);

        assertSame(cached, result);
        verify(goodsService, never()).getById(1L);
        verify(executor, never()).submit(any(FutureTask.class));
    }

    /**
     * 验证商品详情缓存未命中时查询数据库并回填 Redis。
     */
    @Test
    void getGoodsDetailDataCachesDatabaseResult() {
        DirectThreadPoolTaskExecutor executor = new DirectThreadPoolTaskExecutor();
        GoodsDetailServiceImpl service = new GoodsDetailServiceImpl(goodsService, goodsSpecificationService,
                goodsProductService, goodsAttributeService, executor, redisCache);
        Goods goods = new Goods();
        goods.setId(2L);
        List<SpecificationVO> specifications = List.of(new SpecificationVO());
        List<GoodsProduct> products = List.of(new GoodsProduct());
        List<GoodsAttribute> attributes = List.of(new GoodsAttribute());
        when(redisCache.getCacheObject(GOODS_DETAIL_CACHE.getKey(2L))).thenReturn(null);
        when(goodsService.getById(2L)).thenReturn(goods);
        when(goodsSpecificationService.getSpecificationVOList(2L)).thenReturn(specifications);
        when(goodsProductService.list(org.mockito.ArgumentMatchers.<Wrapper<GoodsProduct>>any())).thenReturn(products);
        when(goodsAttributeService.list(org.mockito.ArgumentMatchers.<Wrapper<GoodsAttribute>>any())).thenReturn(attributes);

        GoodsDetailResponseVO result = service.getGoodsDetailData(2L);

        verify(redisCache).setCacheObject(GOODS_DETAIL_CACHE.getKey(2L), result,
                GOODS_DETAIL_CACHE.getExpireSecond());
        assertSame(goods, result.getInfo());
        assertSame(specifications, result.getSpecificationList());
        assertSame(products, result.getProductList());
        assertSame(attributes, result.getAttributes());
    }

    /**
     * 测试用同步线程池，避免单元测试依赖真实异步调度。
     */
    private static final class DirectThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

        /**
         * 直接在当前线程执行 FutureTask。
         *
         * @param task 待执行任务
         * @return 原 FutureTask
         */
        @Override
        public <T> java.util.concurrent.Future<T> submit(Callable<T> task) {
            FutureTask<T> futureTask = new FutureTask<>(task);
            futureTask.run();
            return futureTask;
        }

        /**
         * 直接在当前线程执行 FutureTask。
         *
         * @param task 待执行任务
         * @return 原 FutureTask
         */
        @Override
        public java.util.concurrent.Future<?> submit(Runnable task) {
            FutureTask<?> futureTask = new FutureTask<>(task, null);
            futureTask.run();
            return futureTask;
        }
    }
}
