package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.entity.shop.GoodsAttribute;
import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.service.shop.*;
import com.wayn.common.core.vo.GoodsDetailResponseVO;
import com.wayn.common.core.vo.SpecificationVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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
    private ThreadPoolTaskExecutor commonThreadPoolTaskExecutor;

    // @Cacheable(value = "goods_detail_cache_#600", unless = "#result == null")
    @Override
    public GoodsDetailResponseVO getGoodsDetailData(Long goodsId) {
        GoodsDetailResponseVO responseVO = new GoodsDetailResponseVO();
        Callable<List<SpecificationVO>> specificationCall = () -> iGoodsSpecificationService.getSpecificationVOList(goodsId);
        Callable<List<GoodsProduct>> productCall = () -> iGoodsProductService.list(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        Callable<List<GoodsAttribute>> attrCall = () -> iGoodsAttributeService.list(new QueryWrapper<GoodsAttribute>().eq("goods_id", goodsId));

        FutureTask<List<SpecificationVO>> specificationTask = new FutureTask<>(specificationCall);
        FutureTask<List<GoodsProduct>> productTask = new FutureTask<>(productCall);
        FutureTask<List<GoodsAttribute>> attrTask = new FutureTask<>(attrCall);
        commonThreadPoolTaskExecutor.submit(specificationTask);
        commonThreadPoolTaskExecutor.submit(productTask);
        commonThreadPoolTaskExecutor.submit(attrTask);
        try {
            responseVO.setInfo(iGoodsService.getById(goodsId));
            responseVO.setProductList(productTask.get());
            responseVO.setSpecificationList(specificationTask.get());
            responseVO.setAttributes(attrTask.get());
            return responseVO;
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
