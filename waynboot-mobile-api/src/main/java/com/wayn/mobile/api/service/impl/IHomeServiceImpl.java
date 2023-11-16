package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.convert.MallConfigConvert;
import com.wayn.common.core.domain.shop.Banner;
import com.wayn.common.core.domain.shop.Diamond;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.shop.vo.HomeIndexResponseVO;
import com.wayn.common.core.domain.shop.vo.MallConfigResponseVO;
import com.wayn.common.core.domain.shop.vo.RecommonGoodsResponseVO;
import com.wayn.common.core.service.shop.IBannerService;
import com.wayn.common.core.service.shop.IDiamondService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.mobile.api.service.IHomeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@CacheConfig(keyGenerator = "cacheKeyGenerator")
@AllArgsConstructor
public class IHomeServiceImpl implements IHomeService {

    private IBannerService iBannerService;
    private IGoodsService iGoodsService;
    private IDiamondService iDiamondService;
    private ThreadPoolTaskExecutor commonThreadPoolTaskExecutor;


    @Cacheable(value = "home_index_cache#600", unless = "#result == null")
    @Override
    public HomeIndexResponseVO index() {
        HomeIndexResponseVO responseVO = new HomeIndexResponseVO();
        try {
            List<CompletableFuture<Void>> list = new ArrayList<>(4);
            CompletableFuture<Void> f1 = CompletableFuture.supplyAsync(
                            () -> iBannerService.list(Wrappers.lambdaQuery(Banner.class).eq(Banner::getStatus, 0).orderByAsc(Banner::getSort)), commonThreadPoolTaskExecutor)
                    .thenAccept(responseVO::setBannerList);
            CompletableFuture<Void> f2 = CompletableFuture.supplyAsync(
                            () -> iDiamondService.list(Wrappers.lambdaQuery(Diamond.class).orderByAsc(Diamond::getSort).last("limit 10")), commonThreadPoolTaskExecutor)
                    .thenAccept(responseVO::setDiamondList);
            CompletableFuture<Void> f3 = CompletableFuture.supplyAsync(
                            () -> iGoodsService.selectHomeIndexGoods(Goods.builder().isNew(true).build()), commonThreadPoolTaskExecutor)
                    .thenAccept(responseVO::setNewGoodsList);
            CompletableFuture<Void> f4 = CompletableFuture.supplyAsync(
                            () -> iGoodsService.selectHomeIndexGoods(Goods.builder().isHot(true).build()), commonThreadPoolTaskExecutor)
                    .thenAccept(responseVO::setHotGoodsList);
            list.add(f1);
            list.add(f2);
            list.add(f3);
            list.add(f4);
            CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
            return responseVO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null ;
    }

    @Override
    public RecommonGoodsResponseVO listGoodsPage(Page<Goods> page) {
        RecommonGoodsResponseVO responseVO = new RecommonGoodsResponseVO();
        IPage<Goods> goodsIPage = iGoodsService.listPage(page, new Goods());
        responseVO.setData(goodsIPage.getRecords());
        return responseVO;

    }

    @Override
    public MallConfigResponseVO mallConfig() {
        return MallConfigConvert.convertMallConfig();
    }

}
