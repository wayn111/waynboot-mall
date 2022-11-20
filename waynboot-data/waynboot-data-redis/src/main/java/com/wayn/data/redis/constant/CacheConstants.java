package com.wayn.data.redis.constant;

/**
 * 缓存常量
 */
public class CacheConstants {

    /**
     * 缓存前缀, 统一项目缓存前缀
     */
    public static final String CACHE_PREFIX = "waynboot:";

    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = CACHE_PREFIX + "login_tokens:";


    public static final String SHOP_HOME_INDEX_HASH = CACHE_PREFIX + "shop_home_index_hash";
    public static final String SHOP_HOME_INDEX_HASH_EXPIRATION_FIELD = "expire_time";

}
