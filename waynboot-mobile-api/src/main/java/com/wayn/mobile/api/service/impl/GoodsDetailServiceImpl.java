package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.domain.shop.GoodsAttribute;
import com.wayn.common.core.domain.shop.GoodsProduct;
import com.wayn.common.core.service.shop.IGoodsAttributeService;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IGoodsSpecificationService;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IGoodsDetailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Slf4j
@Service
@AllArgsConstructor
public class GoodsDetailServiceImpl implements IGoodsDetailService {


    private IGoodsService iGoodsService;
    private IGoodsSpecificationService iGoodsSpecificationService;
    private IGoodsProductService iGoodsProductService;
    private IGoodsAttributeService iGoodsAttributeService;
    private ThreadPoolTaskExecutor homeThreadPoolTaskExecutor;

    @Override
    public R getGoodsDetailData(Long goodsId) {
        R success = R.success();
        Callable<Object> specificationCall = () -> iGoodsSpecificationService.getSpecificationVOList(goodsId);
        Callable<List<GoodsProduct>> productCall = () -> iGoodsProductService.list(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        Callable<List<GoodsAttribute>> attrCall = () -> iGoodsAttributeService.list(new QueryWrapper<GoodsAttribute>().eq("goods_id", goodsId));

        FutureTask<Object> specificationTask = new FutureTask<>(specificationCall);
        FutureTask<List<GoodsProduct>> productTask = new FutureTask<>(productCall);
        FutureTask<List<GoodsAttribute>> attrTask = new FutureTask<>(attrCall);
        homeThreadPoolTaskExecutor.submit(specificationTask);
        homeThreadPoolTaskExecutor.submit(productTask);
        homeThreadPoolTaskExecutor.submit(attrTask);
        try {
            success.add("info", iGoodsService.getById(goodsId));
            success.add("specificationList", specificationTask.get());
            success.add("productList", productTask.get());
            success.add("attributes", attrTask.get());
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage(), e);
        }
        return success;
    }
}
