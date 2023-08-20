package com.wayn.data.redis.constant;

import lombok.Getter;

/**
 * @author: waynaqua
 * @date: 2023/8/10 22:28
 */
@Getter
public enum RedisKeyEnum {

    ES_SYNC_CACHE(CacheConstants.CACHE_PREFIX + "es_sync_cache", 3600),
    CART_LOCK(CacheConstants.CACHE_PREFIX + "cart_lock:", 3600),
    EMAIL_CONSUMER_MAP(CacheConstants.CACHE_PREFIX + "email_consumer_map", 60),
    ORDER_CONSUMER_MAP(CacheConstants.CACHE_PREFIX + "order_consumer_map", 60),
    UNPAID_ORDER_CONSUMER_MAP(CacheConstants.CACHE_PREFIX + "unpaid_order_consumer_map", 60),
    ;

    private String key;
    private Integer expireSecond;

    RedisKeyEnum(String key, Integer expireSecond) {
        this.key = key;
        this.expireSecond = expireSecond;
    }

    public String getKey(Object param) {
        return this.getKey() + param;
    }

}
