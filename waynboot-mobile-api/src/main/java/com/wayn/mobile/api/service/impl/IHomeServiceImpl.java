package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Banner;
import com.wayn.common.core.domain.shop.Category;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.service.shop.IBannerService;
import com.wayn.common.core.service.shop.ICategoryService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IHomeService;
import com.wayn.mobile.framework.redis.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class IHomeServiceImpl implements IHomeService {

    private static final String INDEX_DATA = "shop_index_data";


    @Autowired
    private IBannerService iBannerService;

    @Autowired
    private ICategoryService iCategoryService;

    @Autowired
    private IGoodsService iGoodsService;

    @Autowired
    private RedisCache redisCache;

    @Override
    public R getHomeIndexData() {
        if (redisCache.existsKey(INDEX_DATA)) {
            return redisCache.getCacheObject(INDEX_DATA);
        }
        R success = R.success();
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), r -> new Thread(r, "首页线程"));
        Callable<List<Banner>> bannerCall = () -> iBannerService.list(new QueryWrapper<Banner>().eq("status", 0).orderByAsc("sort_order"));
        Callable<List<Category>> categoryCall = () -> iCategoryService.list(new QueryWrapper<Category>().eq("level", "L1").orderByAsc("sort_order"));
        Callable<List<Goods>> newGoodsCall = () -> iGoodsService.list(new QueryWrapper<Goods>()
                .eq("is_new", true)
                .eq("is_on_sale", true)
                .orderByAsc("create_time")
                .last("limit 6"));
        Callable<List<Goods>> hotGoodsCall = () -> iGoodsService.list(new QueryWrapper<Goods>()
                .eq("is_hot", true)
                .eq("is_on_sale", true)
                .orderByAsc("create_time")
                .last("limit 6"));
        FutureTask<List<Banner>> bannerTask = new FutureTask<>(bannerCall);
        FutureTask<List<Category>> categoryTask = new FutureTask<>(categoryCall);
        FutureTask<List<Goods>> newGoodsTask = new FutureTask<>(newGoodsCall);
        FutureTask<List<Goods>> hotGoodsTask = new FutureTask<>(hotGoodsCall);
        poolExecutor.submit(bannerTask);
        poolExecutor.submit(categoryTask);
        poolExecutor.submit(newGoodsTask);
        poolExecutor.submit(hotGoodsTask);
        try {
            success.add("bannerList", bannerTask.get());
            success.add("categoryList", categoryTask.get());
            success.add("newGoodsList", newGoodsTask.get());
            success.add("hotGoodsList", hotGoodsTask.get());
            redisCache.setCacheObject(INDEX_DATA, success, 10, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            poolExecutor.shutdown();
        }
        return success;
    }


    @Override
    public R getHomeIndexDataCompletableFuture() {
        if (redisCache.existsKey(INDEX_DATA)) {
            return redisCache.getCacheObject(INDEX_DATA);
        }
        R success = R.success();
        List<CompletableFuture<Void>> list = new ArrayList();
        CompletableFuture<Void> f1 = CompletableFuture.supplyAsync(() -> iBannerService.list(new QueryWrapper<Banner>().eq("status", 0).orderByAsc("sort_order")))
                .thenAccept(data -> success.add("bannerList", data));
        CompletableFuture<Void> f2 = CompletableFuture.supplyAsync(() -> iCategoryService.list(new QueryWrapper<Category>().eq("level", "L1").orderByAsc("sort_order")))
                .thenAccept(data -> success.add("categoryList", data));
        CompletableFuture<Void> f3 = CompletableFuture.supplyAsync(() -> iGoodsService.list(new QueryWrapper<Goods>()
                .eq("is_new", true)
                .eq("is_on_sale", true)
                .orderByAsc("create_time")
                .last("limit 6")))
                .thenAccept(data -> success.add("newGoodsList", data));
        CompletableFuture<Void> f4 = CompletableFuture.supplyAsync(() -> iGoodsService.list(new QueryWrapper<Goods>()
                .eq("is_hot", true)
                .eq("is_on_sale", true)
                .orderByAsc("create_time")
                .last("limit 6")))
                .thenAccept(data -> success.add("hotGoodsList", data));
        list.add(f1);
        list.add(f2);
        list.add(f3);
        list.add(f4);
        CompletableFuture.allOf(list.toArray(new CompletableFuture[list.size()])).join();
        redisCache.setCacheObject(INDEX_DATA, success, 10, TimeUnit.MINUTES);
        return success;
    }

    @Override
    public R listGoodsPage(Page<Goods> page) {
        IPage<Goods> goodsIPage = iGoodsService.listPage(page, new Goods());
        return R.success().add("data", goodsIPage.getRecords());
    }

}
