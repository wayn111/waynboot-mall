package com.wayn.domain.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.domain.api.goods.entity.GoodsAttribute;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.goods.service.IGoodsAttributeService;
import com.wayn.domain.api.goods.service.IGoodsDetailService;
import com.wayn.domain.api.goods.service.IGoodsProductService;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.domain.api.goods.service.IGoodsSpecificationService;
import com.wayn.domain.api.goods.response.SpecificationVO;
import com.wayn.domain.api.goods.response.GoodsDetailResponseVO;
import com.wayn.data.redis.manager.RedisCache;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static com.wayn.data.redis.constant.RedisKeyEnum.GOODS_DETAIL_CACHE;

/**
 * 商品详情查询服务。
 * 面向移动端高频详情读请求，优先读取 Redis 聚合缓存，缓存未命中时并发查询商品、规格、货品和属性后回填缓存。
 */
@Slf4j
@Service
@AllArgsConstructor
public class GoodsDetailServiceImpl implements IGoodsDetailService {


    private final IGoodsService iGoodsService;
    private final IGoodsSpecificationService iGoodsSpecificationService;
    private final IGoodsProductService iGoodsProductService;
    private final IGoodsAttributeService iGoodsAttributeService;
    private final ThreadPoolTaskExecutor commonThreadPoolTaskExecutor;
    private final RedisCache redisCache;

    /**
     * 获取商品详情聚合数据。
     * 商品详情包含库存列表，因此下单扣减、取消回补和管理端商品编辑后必须主动失效该缓存。
     *
     * @param goodsId 商品 ID
     * @return 商品详情聚合数据
     */
    @Override
    public GoodsDetailResponseVO getGoodsDetailData(Long goodsId) {
        String cacheKey = GOODS_DETAIL_CACHE.getKey(goodsId);
        GoodsDetailResponseVO cached = redisCache.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }

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
            redisCache.setCacheObject(cacheKey, responseVO, GOODS_DETAIL_CACHE.getExpireSecond());
            return responseVO;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("商品详情查询被中断", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("商品详情查询失败", e);
        }
    }
}
