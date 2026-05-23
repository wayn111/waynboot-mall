package com.wayn.common.core.service.shop.support.order;

import com.wayn.common.core.entity.shop.Cart;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis Lua 库存预占支撑服务。
 * 该组件只作为热点 SKU 进入 MySQL 条件冻结前的并发闸门，最终库存一致性仍由 MySQL 条件更新和库存流水保证。
 */
@Slf4j
@Service
@AllArgsConstructor
public class RedisStockPreDeductSupport {

    static final long RESERVE_SUCCESS = 1L;
    static final long RESERVE_INSUFFICIENT = -1L;
    static final long RESERVE_NOT_INITIALIZED = -2L;
    static final int RESERVATION_TTL_SECONDS = 30;

    private final RedisCache redisCache;

    /**
     * 预占下单购物车中的 SKU 库存。
     * 如果 Redis 热点库存快照未初始化，直接降级为空预占并交给 MySQL 条件冻结兜底。
     *
     * @param orderSn 订单号
     * @param checkedGoodsList 已勾选购物车商品
     * @return Redis 预占释放句柄
     */
    public RedisStockReservation preDeduct(String orderSn, List<Cart> checkedGoodsList) {
        if (CollectionUtils.isEmpty(checkedGoodsList)) {
            return RedisStockReservation.empty();
        }
        Map<Long, Integer> requiredNumberMap = aggregateRequiredNumber(checkedGoodsList);
        List<RedisStockReservation.ReservedStockItem> reservedItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : requiredNumberMap.entrySet()) {
            RedisStockReserveResult reserveResult = reserveSingleProduct(orderSn, entry.getKey(), entry.getValue());
            if (reserveResult.notInitialized()) {
                releaseReservedItems(reservedItems);
                log.info("Redis 库存快照未初始化，跳过预占, orderSn={}, productId={}",
                        orderSn, entry.getKey());
                return RedisStockReservation.empty();
            }
            if (reserveResult.insufficient()) {
                releaseReservedItems(reservedItems);
                throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "Redis 库存预占不足");
            }
            if (!reserveResult.success()) {
                releaseReservedItems(reservedItems);
                throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "Redis 库存预占失败");
            }
            reservedItems.add(reserveResult.reservedItem());
        }
        return new RedisStockReservation(redisCache, reservedItems);
    }

    /**
     * 预占单个 SKU 库存。
     * 路由结果和预占数量被固化到 ReservedStockItem，确保后续释放时使用和预占完全相同的 Redis Key。
     *
     * @param orderSn 订单号
     * @param productId 商品货品 ID
     * @param requiredNumber 预占数量
     * @return 单 SKU 预占结果
     */
    private RedisStockReserveResult reserveSingleProduct(String orderSn, Long productId, Integer requiredNumber) {
        RedisStockKeyRoute keyRoute = routeStockKeys(orderSn, productId);
        Long result = redisCache.luaReserveStock(keyRoute.availableKey(), keyRoute.reservedKey(), keyRoute.orderKey(),
                requiredNumber, RESERVATION_TTL_SECONDS);
        RedisStockReservation.ReservedStockItem reservedItem = new RedisStockReservation.ReservedStockItem(productId,
                keyRoute.reservedKey(), keyRoute.orderKey(), requiredNumber);
        return new RedisStockReserveResult(result, reservedItem);
    }

    /**
     * 按 SKU 聚合预占数量。
     *
     * @param checkedGoodsList 已勾选购物车商品
     * @return SKU 到预占数量的映射
     */
    private Map<Long, Integer> aggregateRequiredNumber(List<Cart> checkedGoodsList) {
        Map<Long, Integer> requiredNumberMap = new LinkedHashMap<>();
        for (Cart checkedGoods : checkedGoodsList) {
            if (invalidCheckedGoods(checkedGoods)) {
                throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
            }
            requiredNumberMap.merge(checkedGoods.getProductId(), checkedGoods.getNumber(), Integer::sum);
        }
        return requiredNumberMap;
    }

    /**
     * 判断购物车项是否无法参与 Redis 预占。
     * Redis 预占只能处理明确的 SKU 和正数数量，非法数据必须在进入 Lua 前阻断，避免写出无效订单预占 Key。
     *
     * @param checkedGoods 已勾选购物车商品
     * @return true=非法购物车项
     */
    private boolean invalidCheckedGoods(Cart checkedGoods) {
        return checkedGoods == null || checkedGoods.getProductId() == null
                || checkedGoods.getNumber() == null || checkedGoods.getNumber() <= 0;
    }

    /**
     * 释放已经成功预占的 SKU，避免部分 SKU 预占失败时遗留 Redis 并发闸门占用。
     *
     * @param reservedItems 已成功预占的 SKU
     */
    private void releaseReservedItems(List<RedisStockReservation.ReservedStockItem> reservedItems) {
        new RedisStockReservation(redisCache, reservedItems).release();
    }

    /**
     * 根据 Redis 分桶配置选择本次预占使用的 Key。
     * 未配置分桶时仍走 SKU 维度旧 Key，保证没有预热任务时可以平滑降级。
     *
     * @param orderSn 订单号
     * @param productId 商品货品 ID
     * @return 本次 Redis 预占 Key 路由
     */
    private RedisStockKeyRoute routeStockKeys(String orderSn, Long productId) {
        Integer bucketCount = redisCache.getCacheObject(RedisStockKeySupport.bucketCountKey(productId));
        if (bucketCount == null || bucketCount <= 1) {
            return new RedisStockKeyRoute(RedisStockKeySupport.availableKey(productId),
                    RedisStockKeySupport.reservedKey(productId), RedisStockKeySupport.orderKey(orderSn, productId));
        }
        int bucketIndex = RedisStockBucketRouter.route(orderSn, bucketCount);
        return new RedisStockKeyRoute(RedisStockKeySupport.bucketAvailableKey(productId, bucketIndex),
                RedisStockKeySupport.bucketReservedKey(productId, bucketIndex),
                RedisStockKeySupport.bucketOrderKey(orderSn, productId, bucketIndex));
    }

    /**
     * Redis 库存 Key 路由结果。
     * 预占和释放必须使用同一组 Key，因此在预占成功后把路由结果固化到 RedisStockReservation 中。
     */
    private record RedisStockKeyRoute(String availableKey, String reservedKey, String orderKey) {
    }

    /**
     * Redis Lua 预占返回结果。
     * 用具名方法表达 Lua 返回码语义，避免主流程直接比较魔法返回值导致分支含义不清。
     */
    private record RedisStockReserveResult(Long code, RedisStockReservation.ReservedStockItem reservedItem) {

        /**
         * 是否预占成功。
         *
         * @return true=预占成功
         */
        private boolean success() {
            return code != null && code == RESERVE_SUCCESS;
        }

        /**
         * 是否库存不足。
         *
         * @return true=库存不足
         */
        private boolean insufficient() {
            return code != null && code == RESERVE_INSUFFICIENT;
        }

        /**
         * 是否 Redis 快照未初始化。
         *
         * @return true=快照未初始化
         */
        private boolean notInitialized() {
            return code != null && code == RESERVE_NOT_INITIALIZED;
        }
    }
}
