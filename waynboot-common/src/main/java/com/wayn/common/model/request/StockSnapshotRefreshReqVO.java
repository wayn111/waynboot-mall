package com.wayn.common.model.request;

import lombok.Data;

import java.util.List;

/**
 * Redis 库存快照刷新请求。
 * 支持指定 SKU 刷新，也支持按最近更新 SKU 批量预热。
 */
@Data
public class StockSnapshotRefreshReqVO {

    /**
     * 指定刷新的商品货品 ID。
     */
    private List<Long> productIds;

    /**
     * 最近更新 SKU 扫描数量。
     */
    private Integer limit;

    /**
     * 热点 SKU 分桶数量。
     */
    private Integer bucketCount;
}
