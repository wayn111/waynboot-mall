package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.domain.shop.Banner;
import com.wayn.common.core.domain.shop.Category;
import com.wayn.common.core.service.shop.IBannerService;
import com.wayn.common.core.service.shop.ICategoryService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.*;

@RestController
@RequestMapping("home")
public class HomeController {

    @Autowired
    private IBannerService iBannerService;

    @Autowired
    private ICategoryService iCategoryService;

    @PostMapping("index")
    public R getInfo() {
        R success = R.success();
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), r -> new Thread(r,"首页线程"));
        Callable<List<Banner>> bannerCall = () -> iBannerService.list(new QueryWrapper<Banner>().eq("status", 0).orderByAsc("sort_order"));
        Callable<List<Category>> categoryCall = () -> iCategoryService.list(new QueryWrapper<Category>().eq("level", "L1").orderByAsc("sort_order"));
        FutureTask<List<Banner>> bannerTask = new FutureTask<>(bannerCall);
        poolExecutor.submit(bannerTask);
        FutureTask<List<Category>> categoryTask = new FutureTask<>(categoryCall);
        poolExecutor.submit(categoryTask);
        try {
            success.add("bannerList", bannerTask.get());
            success.add("categoryList", categoryTask.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            poolExecutor.shutdown();
        }
        return success;
    }
}

