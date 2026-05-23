package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.entity.shop.Banner;
import com.wayn.common.core.entity.shop.Diamond;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.service.shop.IBannerService;
import com.wayn.common.core.service.shop.IDiamondService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.model.response.HomeIndexResponseVO;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IHomeServiceImplTest {

    /**
     * 验证首页聚合接口会分别加载轮播图、金刚区、新品和热品四个区块。
     */
    @Test
    void indexAggregatesHomeBlocks() {
        IBannerService bannerService = mock(IBannerService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        IDiamondService diamondService = mock(IDiamondService.class);
        IHomeServiceImpl service = new IHomeServiceImpl(bannerService, goodsService, diamondService,
                new DirectThreadPoolTaskExecutor());
        List<Banner> banners = List.of(new Banner());
        List<Diamond> diamonds = List.of(new Diamond());
        List<Goods> newGoods = List.of(buildGoods(true, false));
        List<Goods> hotGoods = List.of(buildGoods(false, true));
        when(bannerService.list(org.mockito.ArgumentMatchers.<Wrapper<Banner>>any())).thenReturn(banners);
        when(diamondService.list(org.mockito.ArgumentMatchers.<Wrapper<Diamond>>any())).thenReturn(diamonds);
        when(goodsService.selectHomeIndexGoods(argThat(goods -> goods != null && Boolean.TRUE.equals(goods.getIsNew()))))
                .thenReturn(newGoods);
        when(goodsService.selectHomeIndexGoods(argThat(goods -> goods != null && Boolean.TRUE.equals(goods.getIsHot()))))
                .thenReturn(hotGoods);

        HomeIndexResponseVO responseVO = service.index();

        assertSame(banners, responseVO.getBannerList());
        assertSame(diamonds, responseVO.getDiamondList());
        assertSame(newGoods, responseVO.getNewGoodsList());
        assertSame(hotGoods, responseVO.getHotGoodsList());
    }

    /**
     * 验证任一区块加载失败时首页返回 null，避免缓存半成品首页数据。
     */
    @Test
    void indexReturnsNullWhenAnyHomeBlockFails() {
        IBannerService bannerService = mock(IBannerService.class);
        IGoodsService goodsService = mock(IGoodsService.class);
        IDiamondService diamondService = mock(IDiamondService.class);
        IHomeServiceImpl service = new IHomeServiceImpl(bannerService, goodsService, diamondService,
                new DirectThreadPoolTaskExecutor());
        when(bannerService.list(org.mockito.ArgumentMatchers.<Wrapper<Banner>>any()))
                .thenThrow(new IllegalStateException("banner query failed"));

        HomeIndexResponseVO responseVO = service.index();

        assertNull(responseVO);
    }

    /**
     * 推荐商品分页底层返回 null 时应兜底为空列表。
     * Controller 会直接对返回值执行 stream，服务层需要保证集合返回值稳定非 null。
     */
    @Test
    void listGoodsPageReturnsEmptyListWhenPageResultIsNull() {
        IGoodsService goodsService = mock(IGoodsService.class);
        IHomeServiceImpl service = new IHomeServiceImpl(mock(IBannerService.class), goodsService,
                mock(IDiamondService.class), new DirectThreadPoolTaskExecutor());
        when(goodsService.listPage(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(null);

        List<Goods> goodsList = service.listGoodsPage(new Page<>(1, 10));

        assertTrue(goodsList.isEmpty());
    }

    /**
     * 分页参数为空时直接返回空列表，不触发底层商品查询。
     */
    @Test
    void listGoodsPageReturnsEmptyListWhenPageIsNull() {
        IGoodsService goodsService = mock(IGoodsService.class);
        IHomeServiceImpl service = new IHomeServiceImpl(mock(IBannerService.class), goodsService,
                mock(IDiamondService.class), new DirectThreadPoolTaskExecutor());

        List<Goods> goodsList = service.listGoodsPage(null);

        assertTrue(goodsList.isEmpty());
        verify(goodsService, never()).listPage(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    /**
     * 构建商品测试对象。
     *
     * @param isNew 是否新品
     * @param isHot 是否热品
     * @return 商品对象
     */
    private Goods buildGoods(boolean isNew, boolean isHot) {
        Goods goods = new Goods();
        goods.setIsNew(isNew);
        goods.setIsHot(isHot);
        return goods;
    }

    /**
     * 测试用同步执行器。
     * 首页服务生产环境并行加载多个区块，单测中同步执行可以稳定验证响应组装结果。
     */
    private static final class DirectThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

        /**
         * 直接在当前线程执行任务。
         *
         * @param task 待执行任务
         */
        @Override
        public void execute(Runnable task) {
            task.run();
        }
    }
}
