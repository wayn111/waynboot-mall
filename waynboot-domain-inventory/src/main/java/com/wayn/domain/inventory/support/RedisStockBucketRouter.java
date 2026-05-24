package com.wayn.domain.inventory.support;

import org.apache.commons.lang3.StringUtils;

/**
 * Redis 热点库存分桶路由器。
 * 只负责把同一热点 SKU 的不同订单稳定路由到固定桶，降低单个 Redis Key 的并发竞争。
 */
public final class RedisStockBucketRouter {

    private RedisStockBucketRouter() {
    }

    /**
     * 根据订单号路由分桶。
     * 优先使用订单号末尾数字，便于排查和复现；没有数字时退化为字符串 hash。
     *
     * @param orderSn 订单号
     * @param bucketCount 分桶数量
     * @return 分桶下标
     */
    static int route(String orderSn, int bucketCount) {
        if (bucketCount <= 1) {
            return 0;
        }
        String numericSuffix = StringUtils.defaultString(orderSn).replaceAll("^.*?(\\d+)$", "$1");
        if (StringUtils.isNumeric(numericSuffix)) {
            return Math.floorMod(Long.valueOf(numericSuffix).hashCode(), bucketCount);
        }
        return Math.floorMod(StringUtils.defaultString(orderSn).hashCode(), bucketCount);
    }
}
