package com.wayn.domain.inventory.support;

import lombok.Data;

/**
 * Redis 库存快照刷新结果。
 * 供定时任务、管理端或测试判断本次预热了哪个 SKU、可售库存和分桶数量。
 */
@Data
public class RedisStockSnapshotRefreshResult {

    /**
     * 商品货品 ID。
     */
    private Long productId;

    /**
     * 写入 Redis 的可售库存快照。
     */
    private Integer availableStock;

    /**
     * 本次写入的分桶数量。
     */
    private Integer bucketCount;

    /**
     * 成功写入的分桶 Key 数量。
     */
    private Integer refreshedBucketCount;
}
