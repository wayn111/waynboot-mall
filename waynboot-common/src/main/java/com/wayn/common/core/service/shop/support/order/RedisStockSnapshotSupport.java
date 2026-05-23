package com.wayn.common.core.service.shop.support.order;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Redis 库存快照预热与刷新服务。
 * 从 MySQL SKU 库存生成 Redis 可售库存快照和热点 SKU 分桶快照；Redis 只承担入口削峰，最终一致性仍由 MySQL 条件更新和库存流水兜底。
 */
@Service
@AllArgsConstructor
public class RedisStockSnapshotSupport {

    private static final int DEFAULT_BUCKET_COUNT = 1;
    private static final int MAX_BUCKET_COUNT = 64;
    private static final int DEFAULT_REFRESH_LIMIT = 500;

    private final IGoodsProductService goodsProductService;
    private final RedisCache redisCache;

    /**
     * 刷新单个 SKU 的 Redis 库存快照。
     *
     * @param productId 商品货品 ID
     * @param bucketCount 分桶数量，热点 SKU 可设置大于 1
     * @return 刷新结果
     */
    public RedisStockSnapshotRefreshResult refreshProductSnapshot(Long productId, int bucketCount) {
        if (productId == null) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
        GoodsProduct product = goodsProductService.getById(productId);
        if (product == null) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
        return refreshProductSnapshot(product, bucketCount);
    }

    /**
     * 批量刷新指定 SKU 的 Redis 库存快照。
     *
     * @param productIds 商品货品 ID 集合
     * @param bucketCount 分桶数量
     * @return 刷新结果列表
     */
    public List<RedisStockSnapshotRefreshResult> refreshProductSnapshots(Collection<Long> productIds, int bucketCount) {
        List<Long> distinctProductIds = normalizeProductIds(productIds);
        if (CollectionUtils.isEmpty(distinctProductIds)) {
            return List.of();
        }
        return refreshProducts(goodsProductService.listByIds(distinctProductIds), bucketCount);
    }

    /**
     * 刷新一批 SKU 库存快照。
     * 该方法用于 XXL-Job 定时预热，避免 Redis 快照缺失导致热点 SKU 每次都降级到 MySQL。
     *
     * @param limit 每批最多刷新数量
     * @param bucketCount 分桶数量
     * @return 刷新结果列表
     */
    public List<RedisStockSnapshotRefreshResult> refreshLatestSnapshots(int limit, int bucketCount) {
        return refreshProducts(listLatestProducts(limit), bucketCount);
    }

    /**
     * 查询最近更新的一批有效 SKU。
     * 只扫描未删除货品，避免把已下架或逻辑删除库存继续预热到 Redis 入口闸门。
     *
     * @param limit 每批最多刷新数量
     * @return 商品货品列表
     */
    private List<GoodsProduct> listLatestProducts(int limit) {
        return goodsProductService.list(Wrappers.lambdaQuery(GoodsProduct.class)
                .eq(GoodsProduct::getDelFlag, false)
                .orderByDesc(GoodsProduct::getUpdateTime)
                .last(limitClause(limit)));
    }

    /**
     * 批量刷新商品货品 Redis 库存快照。
     *
     * @param products 商品货品列表
     * @param bucketCount 分桶数量
     * @return 刷新结果列表
     */
    private List<RedisStockSnapshotRefreshResult> refreshProducts(List<GoodsProduct> products, int bucketCount) {
        if (CollectionUtils.isEmpty(products)) {
            return List.of();
        }
        return products.stream()
                .filter(this::hasProductId)
                .map(product -> refreshProductSnapshot(product, bucketCount))
                .toList();
    }

    /**
     * 按商品货品对象刷新 Redis 快照。
     *
     * @param product 商品货品对象
     * @param bucketCount 原始分桶数量
     * @return 刷新结果
     */
    private RedisStockSnapshotRefreshResult refreshProductSnapshot(GoodsProduct product, int bucketCount) {
        int availableStock = defaultNumber(product.getNumber());
        int safeBucketCount = normalizeBucketCount(bucketCount);
        writeGlobalSnapshot(product.getId(), availableStock, safeBucketCount);
        List<Integer> bucketStocks = splitStock(availableStock, safeBucketCount);
        writeBucketSnapshots(product.getId(), bucketStocks);
        return buildRefreshResult(product.getId(), availableStock, safeBucketCount, bucketStocks.size());
    }

    /**
     * 写入 SKU 维度全局库存快照。
     * 全局库存 Key 保留用于无分桶场景和运维排查，分桶数量 Key 决定下单预占是否路由到桶。
     *
     * @param productId 商品货品 ID
     * @param availableStock 可售库存
     * @param safeBucketCount 安全分桶数量
     */
    private void writeGlobalSnapshot(Long productId, int availableStock, int safeBucketCount) {
        redisCache.setCacheObject(RedisStockKeySupport.availableKey(productId), availableStock);
        redisCache.setCacheObject(RedisStockKeySupport.bucketCountKey(productId), safeBucketCount);
    }

    /**
     * 写入热点 SKU 分桶库存快照。
     * 分桶库存总和必须等于全局库存，预扣时按订单号路由单桶，降低热点 SKU 单 Key 竞争。
     *
     * @param productId 商品货品 ID
     * @param bucketStocks 分桶库存
     */
    private void writeBucketSnapshots(Long productId, List<Integer> bucketStocks) {
        for (int bucketIndex = 0; bucketIndex < bucketStocks.size(); bucketIndex++) {
            redisCache.setCacheObject(RedisStockKeySupport.bucketAvailableKey(productId, bucketIndex),
                    bucketStocks.get(bucketIndex));
        }
    }

    /**
     * 构建 Redis 库存快照刷新结果。
     *
     * @param productId 商品货品 ID
     * @param availableStock 可售库存
     * @param safeBucketCount 安全分桶数量
     * @param refreshedBucketCount 刷新的分桶数量
     * @return 刷新结果
     */
    private RedisStockSnapshotRefreshResult buildRefreshResult(Long productId, int availableStock, int safeBucketCount,
                                                               int refreshedBucketCount) {
        RedisStockSnapshotRefreshResult result = new RedisStockSnapshotRefreshResult();
        result.setProductId(productId);
        result.setAvailableStock(availableStock);
        result.setBucketCount(safeBucketCount);
        result.setRefreshedBucketCount(refreshedBucketCount);
        return result;
    }

    /**
     * 清洗批量刷新输入。
     * 管理端或任务参数可能带重复、空 ID；统一收敛后再查询，避免无意义 SQL 条件。
     *
     * @param productIds 原始商品货品 ID 集合
     * @return 去重后的非空商品货品 ID 列表
     */
    private List<Long> normalizeProductIds(Collection<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return List.of();
        }
        return productIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 判断商品货品是否可刷新快照。
     * 批量查询在异常数据或 Mock 场景下可能返回空对象；没有 productId 时无法构造稳定 Redis Key，必须跳过。
     *
     * @param product 商品货品
     * @return true=可刷新快照
     */
    private boolean hasProductId(GoodsProduct product) {
        return product != null && product.getId() != null;
    }

    /**
     * 构造安全 limit 片段。
     * 快照刷新可能由定时任务触发，统一限制单批扫描数量，避免误配置导致一次加载过多 SKU。
     *
     * @param limit 原始扫描数量
     * @return MyBatis-Plus last 使用的 limit 片段
     */
    private String limitClause(int limit) {
        int safeLimit = limit <= 0 ? DEFAULT_REFRESH_LIMIT : Math.min(limit, DEFAULT_REFRESH_LIMIT);
        return "limit " + safeLimit;
    }

    /**
     * 将 SKU 可售库存均匀拆分到多个桶。
     * 余数优先落到前几个桶，保证所有桶求和等于 MySQL 可售库存。
     *
     * @param availableStock 可售库存
     * @param bucketCount 分桶数量
     * @return 每个桶的库存快照
     */
    private List<Integer> splitStock(int availableStock, int bucketCount) {
        int baseStock = availableStock / bucketCount;
        int remainder = availableStock % bucketCount;
        List<Integer> bucketStocks = new ArrayList<>(bucketCount);
        for (int bucketIndex = 0; bucketIndex < bucketCount; bucketIndex++) {
            bucketStocks.add(baseStock + (bucketIndex < remainder ? 1 : 0));
        }
        return bucketStocks;
    }

    /**
     * 规范化分桶数量，避免误配置导致过多 Redis Key。
     *
     * @param bucketCount 原始分桶数量
     * @return 安全分桶数量
     */
    private int normalizeBucketCount(int bucketCount) {
        if (bucketCount <= 0) {
            return DEFAULT_BUCKET_COUNT;
        }
        return Math.min(bucketCount, MAX_BUCKET_COUNT);
    }

    /**
     * 返回非空库存数量。
     *
     * @param number 原始库存数量
     * @return 非空库存数量
     */
    private int defaultNumber(Integer number) {
        return number == null ? 0 : number;
    }
}
