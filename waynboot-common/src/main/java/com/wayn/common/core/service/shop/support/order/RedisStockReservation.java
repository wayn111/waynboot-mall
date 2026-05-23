package com.wayn.common.core.service.shop.support.order;

import com.wayn.data.redis.manager.RedisCache;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Redis 库存预占释放句柄。
 * 下单线程只把 Redis 作为进入 MySQL 条件冻结前的并发闸门，拿到句柄后必须在 finally 中释放预占量。
 */
@Slf4j
public class RedisStockReservation {

    private static final RedisStockReservation EMPTY = new RedisStockReservation(null, List.of());

    private final RedisCache redisCache;
    private final List<ReservedStockItem> reservedItems;

    /**
     * 创建 Redis 预占释放句柄。
     *
     * @param redisCache Redis 工具
     * @param reservedItems 已预占 SKU 列表
     */
    public RedisStockReservation(RedisCache redisCache, List<ReservedStockItem> reservedItems) {
        this.redisCache = redisCache;
        this.reservedItems = reservedItems;
    }

    /**
     * 返回空预占句柄。
     *
     * @return 空预占句柄
     */
    public static RedisStockReservation empty() {
        return EMPTY;
    }

    /**
     * 释放全部 Redis 预占量。
     * 释放失败只记录日志，不能覆盖 MySQL 条件冻结的真实业务结果。
     */
    public void release() {
        if (redisCache == null || reservedItems.isEmpty()) {
            return;
        }
        for (ReservedStockItem item : reservedItems) {
            try {
                redisCache.luaReleaseReservedStock(item.reservedKey(), item.orderKey());
            } catch (Exception e) {
                log.error("释放 Redis 库存预占失败, productId={}, orderKey={}", item.productId(), item.orderKey(), e);
            }
        }
    }

    /**
     * Redis 预占 SKU 明细。
     *
     * @param productId 商品货品 ID
     * @param reservedKey SKU 预占总量 Key
     * @param orderKey 订单 SKU 预占 Key
     * @param number 预占数量
     */
    public record ReservedStockItem(Long productId, String reservedKey, String orderKey, Integer number) {
    }
}
