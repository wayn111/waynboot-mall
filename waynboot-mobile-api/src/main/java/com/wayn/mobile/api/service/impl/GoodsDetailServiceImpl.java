package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.domain.shop.GoodsAttribute;
import com.wayn.common.core.domain.shop.GoodsProduct;
import com.wayn.common.core.service.shop.*;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IGoodsDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class GoodsDetailServiceImpl implements IGoodsDetailService {


    @Autowired
    private IGoodsService iGoodsService;

    @Autowired
    private IGoodsSpecificationService iGoodsSpecificationService;

    @Autowired
    private IGoodsProductService iGoodsProductService;

    @Autowired
    private IGoodsAttributeService iGoodsAttributeService;

    @Override
    public R getGoodsDetailData(Long goodsId) {
        R success = R.success();
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), r -> new Thread(r, "商品详情线程"));
        Callable<Object> specificationCall = () -> iGoodsSpecificationService.getSpecificationVOList(goodsId);
        Callable<List<GoodsProduct>> productCall =
                () -> iGoodsProductService.list(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        Callable<List<GoodsAttribute>> attrCall =
                () -> iGoodsAttributeService.list(new QueryWrapper<GoodsAttribute>().eq("goods_id", goodsId));

        FutureTask<Object> specificationTask = new FutureTask<>(specificationCall);
        FutureTask<List<GoodsProduct>> productTask = new FutureTask<>(productCall);
        FutureTask<List<GoodsAttribute>> attrTask = new FutureTask<>(attrCall);
        poolExecutor.submit(specificationTask);
        poolExecutor.submit(productTask);
        poolExecutor.submit(attrTask);
        try {
            success.add("info", iGoodsService.getById(goodsId));
            success.add("specificationList", specificationTask.get());
            success.add("productList", productTask.get());
            success.add("attributes", attrTask.get());
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage(), e);;
        } finally {
            poolExecutor.shutdown();
        }
        return success;
    }
}
