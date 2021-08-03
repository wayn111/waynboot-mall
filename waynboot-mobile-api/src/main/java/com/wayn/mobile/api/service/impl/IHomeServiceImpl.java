package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Banner;
import com.wayn.common.core.domain.shop.Diamond;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.service.shop.IBannerService;
import com.wayn.common.core.service.shop.ICategoryService;
import com.wayn.common.core.service.shop.IDiamondService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.mobile.api.service.IHomeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@AllArgsConstructor
public class IHomeServiceImpl implements IHomeService {

    private static final String SHOP_HOME_INDEX_HASH = "shop_home_index_hash";

    private IBannerService iBannerService;
    private ICategoryService iCategoryService;
    private IGoodsService iGoodsService;
    private RedisCache redisCache;
    private RedisTemplate<String, Object> redisTemplate;
    private IDiamondService iDiamondService;
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @Override
    public R getHomeIndexDataCompletableFuture() {
        R success = R.success();
        Map<String, Object> shopHomeIndexHash = redisCache.getCacheMap("shop_home_index_hash");
        if (MapUtils.isNotEmpty(shopHomeIndexHash)) {
            shopHomeIndexHash.forEach(success::add);
            return success;
        }

        List<CompletableFuture<Void>> list = new ArrayList<>(4);
        CompletableFuture<Void> f1 = CompletableFuture.supplyAsync(
                () -> iBannerService.list(Wrappers.lambdaQuery(Banner.class).eq(Banner::getStatus, 0).orderByAsc(Banner::getSort)), threadPoolTaskExecutor)
                .thenAccept(data -> {
                    String key = "bannerList";
                    redisCache.setCacheMapValue(SHOP_HOME_INDEX_HASH, key, data);
                    success.add(key, data);
                });
        CompletableFuture<Void> f2 = CompletableFuture.supplyAsync(
                () -> iDiamondService.list(Wrappers.lambdaQuery(Diamond.class).orderByAsc(Diamond::getSort).last("limit 10")), threadPoolTaskExecutor)
                .thenAccept(data -> {
                    String key = "categoryList";
                    redisCache.setCacheMapValue(SHOP_HOME_INDEX_HASH, key, data);
                    success.add(key, data);
                });
        CompletableFuture<Void> f3 = CompletableFuture.supplyAsync(
                () -> iGoodsService.selectHomeIndexGoods(Goods.builder().isNew(true).build()))
                .thenAccept(data -> {
                    String key = "newGoodsList";
                    redisCache.setCacheMapValue(SHOP_HOME_INDEX_HASH, key, data);
                    success.add(key, data);
                });
        CompletableFuture<Void> f4 = CompletableFuture.supplyAsync(
                () -> iGoodsService.selectHomeIndexGoods(Goods.builder().isHot(true).build()))
                .thenAccept(data -> {
                    String key = "hotGoodsList";
                    redisCache.setCacheMapValue(SHOP_HOME_INDEX_HASH, key, data);
                    success.add(key, data);
                });
        list.add(f1);
        list.add(f2);
        list.add(f3);
        list.add(f4);
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        return success;
    }

    @Override
    public R listGoodsPage(Page<Goods> page) {
        IPage<Goods> goodsIPage = iGoodsService.listPage(page, new Goods());
        return R.success().add("data", goodsIPage.getRecords());
    }

}
