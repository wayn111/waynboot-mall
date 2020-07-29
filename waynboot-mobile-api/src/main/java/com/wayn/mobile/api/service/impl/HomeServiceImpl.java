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
import com.wayn.mobile.api.service.HomeService;
import com.wayn.mobile.framework.redis.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

@Service
public class HomeServiceImpl implements HomeService {

    private static final String INDEX_DATA = "index_data";


    @Autowired
    private IBannerService iBannerService;

    @Autowired
    private ICategoryService iCategoryService;

    @Autowired
    private IGoodsService IGoodsService;

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
        Callable<List<Goods>> newGoodsCall = () -> IGoodsService.list(new QueryWrapper<Goods>()
                .eq("is_new", true)
                .eq("is_on_sale", true)
                .orderByAsc("create_time")
                .last("limit 6"));
        Callable<List<Goods>> hotGoodsCall = () -> IGoodsService.list(new QueryWrapper<Goods>()
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
    public R getGoodsList() {

        return null;
    }

    @Override
    public R listGoodsPage(Page<Goods> page) {
        IPage<Goods> goodsIPage = IGoodsService.listPage(page, new Goods());
        return R.success().add("data", goodsIPage.getRecords());
    }

}
