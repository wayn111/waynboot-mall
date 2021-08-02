package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@AllArgsConstructor
public class IHomeServiceImpl implements IHomeService {

    private static final String SHOP_INDEX_BANNER_LIST = "shop_index_banner_list";
    private static final String SHOP_INDEX_CATEGORY_LIST = "shop_index_category_list";
    private static final String SHOP_INDEX_GOODS_NEW_LIST = "shop_index_goods_new_list";
    private static final String SHOP_INDEX_GOODS_HOT_LIST = "shop_index_goods_hot_list";

    private IBannerService iBannerService;
    private ICategoryService iCategoryService;
    private IGoodsService iGoodsService;
    private RedisCache redisCache;
    private IDiamondService iDiamondService;
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @Override
    public R getHomeIndexDataCompletableFuture() {
        R success = R.success();

        List<CompletableFuture<Void>> list = new ArrayList<>(4);
        CompletableFuture<Void> f1 = CompletableFuture.supplyAsync(() -> iBannerService.list(new QueryWrapper<Banner>()
                        .eq("status", 0)
                        .orderByAsc("sort")), threadPoolTaskExecutor)
                .thenAccept(data -> {
                    redisCache.setCacheObject(SHOP_INDEX_BANNER_LIST, data, 3600, TimeUnit.MINUTES);
                    success.add("bannerList", data);
                });
        CompletableFuture<Void> f2 = CompletableFuture.supplyAsync(() -> iDiamondService.list(new QueryWrapper<Diamond>()
                        .orderByAsc("sort")
                        .last("limit 10")), threadPoolTaskExecutor)
                .thenAccept(data -> {
                    redisCache.setCacheObject(SHOP_INDEX_CATEGORY_LIST, data, 3600, TimeUnit.MINUTES);
                    success.add("categoryList", data);
                });
        CompletableFuture<Void> f3 = CompletableFuture.supplyAsync(() -> iGoodsService.list(new QueryWrapper<Goods>()
                        .eq("is_new", true)
                        .eq("is_on_sale", true)
                        .orderByAsc("create_time")
                        .last("limit 6")), threadPoolTaskExecutor)
                .thenAccept(data -> {
                    redisCache.setCacheObject(SHOP_INDEX_GOODS_NEW_LIST, data, 3600, TimeUnit.MINUTES);
                    success.add("newGoodsList", data);
                });
        CompletableFuture<Void> f4 = CompletableFuture.supplyAsync(() -> iGoodsService.list(new QueryWrapper<Goods>()
                        .eq("is_hot", true)
                        .eq("is_on_sale", true)
                        .orderByAsc("create_time")
                        .last("limit 6")), threadPoolTaskExecutor)
                .thenAccept(data -> {
                    redisCache.setCacheObject(SHOP_INDEX_GOODS_HOT_LIST, data, 3600, TimeUnit.MINUTES);
                    success.add("hotGoodsList", data);
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
