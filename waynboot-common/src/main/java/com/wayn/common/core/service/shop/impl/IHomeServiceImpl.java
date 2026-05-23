package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.convert.MallConfigConvert;
import com.wayn.common.core.entity.shop.Banner;
import com.wayn.common.core.entity.shop.Diamond;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.service.shop.IBannerService;
import com.wayn.common.core.service.shop.IDiamondService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IHomeService;
import com.wayn.common.model.response.HomeIndexResponseVO;
import com.wayn.common.model.response.MallConfigResponseVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 移动端首页聚合服务。
 * 负责把轮播图、金刚区、新品和热品查询编排成首页 VO；具体商品、轮播图、金刚区查询仍下沉到各自 Service，
 * 避免首页入口承载领域查询细节。
 */
@Slf4j
@Service
@CacheConfig(keyGenerator = "cacheKeyGenerator")
@AllArgsConstructor
public class IHomeServiceImpl implements IHomeService {

    private static final int BANNER_STATUS_ENABLED = 0;
    private static final String HOME_DIAMOND_LIMIT = "limit 10";

    private final IBannerService iBannerService;
    private final IGoodsService iGoodsService;
    private final IDiamondService iDiamondService;
    private final ThreadPoolTaskExecutor commonThreadPoolTaskExecutor;


    /**
     * 获取首页聚合数据。
     * 四个首页区块并行加载，任一区块异常都会返回 null，配合缓存 unless 规则避免缓存半成品首页。
     *
     * @return 首页聚合响应；加载失败时返回 null
     */
    @Cacheable(value = "home_index_cache#300", unless = "#result == null")
    @Override
    public HomeIndexResponseVO index() {
        HomeIndexResponseVO responseVO = new HomeIndexResponseVO();
        try {
            await(List.of(
                    loadBannerList(responseVO),
                    loadDiamondList(responseVO),
                    loadNewGoodsList(responseVO),
                    loadHotGoodsList(responseVO)));
            return responseVO;
        } catch (Exception e) {
            log.error("加载首页聚合数据失败", e);
        }
        return null;
    }

    /**
     * 查询首页商品分页数据。
     *
     * @param page 分页对象
     * @return 商品列表
     */
    @Override
    public List<Goods> listGoodsPage(Page<Goods> page) {
        if (page == null) {
            // 商品列表是 Controller 直接 stream 的集合出参，服务层必须保证无效分页不会返回 null。
            return List.of();
        }
        IPage<Goods> goodsIPage = iGoodsService.listPage(page, new Goods());
        if (goodsIPage == null) {
            // 底层分页查询异常兜底为空列表，避免首页推荐接口因空分页对象触发 NPE。
            return List.of();
        }
        return goodsIPage.getRecords();

    }

    /**
     * 获取商城基础配置。
     *
     * @return 商城配置响应
     */
    @Override
    public MallConfigResponseVO mallConfig() {
        return MallConfigConvert.convertMallConfig();
    }

    /**
     * 异步加载轮播图列表。
     *
     * @param responseVO 首页响应对象
     * @return 首页区块异步任务
     */
    private CompletableFuture<Void> loadBannerList(HomeIndexResponseVO responseVO) {
        return supplyAsync(() -> iBannerService.list(Wrappers.lambdaQuery(Banner.class)
                .eq(Banner::getStatus, BANNER_STATUS_ENABLED)
                .orderByAsc(Banner::getSort))).thenAccept(responseVO::setBannerList);
    }

    /**
     * 异步加载首页金刚区列表。
     *
     * @param responseVO 首页响应对象
     * @return 首页区块异步任务
     */
    private CompletableFuture<Void> loadDiamondList(HomeIndexResponseVO responseVO) {
        return supplyAsync(() -> iDiamondService.list(Wrappers.lambdaQuery(Diamond.class)
                .orderByAsc(Diamond::getSort)
                .last(HOME_DIAMOND_LIMIT))).thenAccept(responseVO::setDiamondList);
    }

    /**
     * 异步加载首页新品列表。
     *
     * @param responseVO 首页响应对象
     * @return 首页区块异步任务
     */
    private CompletableFuture<Void> loadNewGoodsList(HomeIndexResponseVO responseVO) {
        return supplyAsync(() -> iGoodsService.selectHomeIndexGoods(Goods.builder().isNew(true).build()))
                .thenAccept(responseVO::setNewGoodsList);
    }

    /**
     * 异步加载首页热品列表。
     *
     * @param responseVO 首页响应对象
     * @return 首页区块异步任务
     */
    private CompletableFuture<Void> loadHotGoodsList(HomeIndexResponseVO responseVO) {
        return supplyAsync(() -> iGoodsService.selectHomeIndexGoods(Goods.builder().isHot(true).build()))
                .thenAccept(responseVO::setHotGoodsList);
    }

    /**
     * 提交首页区块异步查询。
     *
     * @param supplier 查询逻辑
     * @param <T> 查询结果类型
     * @return 异步查询结果
     */
    private <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, commonThreadPoolTaskExecutor);
    }

    /**
     * 等待首页区块全部完成。
     * 任一区块失败时继续抛出异常，保持原有“首页聚合失败返回 null 且不写缓存”的语义。
     *
     * @param futures 首页区块任务列表
     */
    private void await(List<CompletableFuture<Void>> futures) {
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

}
