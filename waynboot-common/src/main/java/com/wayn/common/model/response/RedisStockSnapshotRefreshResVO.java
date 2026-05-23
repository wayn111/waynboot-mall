package com.wayn.common.model.response;

import lombok.Data;

/**
 * Redis 库存快照刷新响应。
 */
@Data
public class RedisStockSnapshotRefreshResVO {

    /**
     * 商品货品 ID。
     */
    private Long productId;

    /**
     * 可售库存快照。
     */
    private Integer availableStock;

    /**
     * 分桶数量。
     */
    private Integer bucketCount;

    /**
     * 已刷新分桶数量。
     */
    private Integer refreshedBucketCount;
}
