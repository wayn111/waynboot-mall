package com.wayn.common.core.service.shop.support.order;

import org.apache.commons.lang3.StringUtils;

/**
 * Redis 库存 Key 构建工具。
 * 统一下单预占、库存快照预热和热点 SKU 分桶使用的 Key 规则，避免多个支撑类各自拼接造成补偿链路失配。
 */
final class RedisStockKeySupport {

    private static final String AVAILABLE_KEY_PREFIX = "trade:stock:available:";
    private static final String RESERVED_KEY_PREFIX = "trade:stock:reserved:";
    private static final String ORDER_KEY_PREFIX = "trade:stock:order:";
    private static final String BUCKET_COUNT_KEY_PREFIX = "trade:stock:bucket:count:";
    private static final String BUCKET_SUFFIX = ":bucket:";

    private RedisStockKeySupport() {
    }

    /**
     * 构建 Redis 可售库存快照 Key。
     *
     * @param productId 商品货品 ID
     * @return Redis Key
     */
    static String availableKey(Long productId) {
        return AVAILABLE_KEY_PREFIX + productId;
    }

    /**
     * 构建热点 SKU 分桶数量 Key。
     *
     * @param productId 商品货品 ID
     * @return Redis Key
     */
    static String bucketCountKey(Long productId) {
        return BUCKET_COUNT_KEY_PREFIX + productId;
    }

    /**
     * 构建分桶可售库存快照 Key。
     *
     * @param productId 商品货品 ID
     * @param bucketIndex 分桶下标
     * @return Redis Key
     */
    static String bucketAvailableKey(Long productId, int bucketIndex) {
        return availableKey(productId) + BUCKET_SUFFIX + bucketIndex;
    }

    /**
     * 构建 SKU 预占总量 Key。
     *
     * @param productId 商品货品 ID
     * @return Redis Key
     */
    static String reservedKey(Long productId) {
        return RESERVED_KEY_PREFIX + productId;
    }

    /**
     * 构建分桶预占总量 Key。
     *
     * @param productId 商品货品 ID
     * @param bucketIndex 分桶下标
     * @return Redis Key
     */
    static String bucketReservedKey(Long productId, int bucketIndex) {
        return reservedKey(productId) + BUCKET_SUFFIX + bucketIndex;
    }

    /**
     * 构建订单 SKU 预占 Key。
     *
     * @param orderSn 订单号
     * @param productId 商品货品 ID
     * @return Redis Key
     */
    static String orderKey(String orderSn, Long productId) {
        return ORDER_KEY_PREFIX + StringUtils.defaultString(orderSn) + ":" + productId;
    }

    /**
     * 构建分桶订单 SKU 预占 Key。
     *
     * @param orderSn 订单号
     * @param productId 商品货品 ID
     * @param bucketIndex 分桶下标
     * @return Redis Key
     */
    static String bucketOrderKey(String orderSn, Long productId, int bucketIndex) {
        return orderKey(orderSn, productId) + BUCKET_SUFFIX + bucketIndex;
    }
}
