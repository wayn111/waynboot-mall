package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.domain.shop.GoodsProduct;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IGoodsSpecificationService;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IGoodsDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

@Service
public class GoodsDetailServiceImpl implements IGoodsDetailService {


    @Autowired
    private IGoodsService iGoodsService;

    @Autowired
    private IGoodsSpecificationService iGoodsSpecificationService;

    @Autowired
    private IGoodsProductService iGoodsProductService;

    @Override
    public R getGoodsDetailData(Long goodsId) {
        R success = R.success();
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), r -> new Thread(r, "商品详情线程"));
        Callable<Object> specificationCall = () -> iGoodsSpecificationService.getSpecificationVoList(goodsId);
        Callable<List<GoodsProduct>> productCall = () -> iGoodsProductService.list(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        FutureTask<Object> specificationTask = new FutureTask<>(specificationCall);
        FutureTask<List<GoodsProduct>> productTask = new FutureTask<>(productCall);
        poolExecutor.submit(specificationTask);
        poolExecutor.submit(productTask);
        success.add("info", iGoodsService.getById(goodsId));
        try {
            success.add("specificationList", specificationTask.get());
            success.add("productList", productTask.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            poolExecutor.shutdown();
        }
        return success;
    }
}
