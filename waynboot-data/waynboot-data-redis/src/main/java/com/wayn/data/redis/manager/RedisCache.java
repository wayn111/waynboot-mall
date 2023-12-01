package com.wayn.data.redis.manager;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * spring redis 工具类
 **/
@Slf4j
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
@AllArgsConstructor
public class RedisCache {
    public RedisTemplate redisTemplate;

    private LettuceConnectionFactory lettuceConnectionFactory;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
    }
    public <T> void setCacheObject(final String key, final T value, final Integer timeout) {
        redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
    }
    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 判断缓存是否存在。
     *
     * @param key 缓存键值
     * @return true=存在；false=不存在
     */
    public boolean existsKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 获取多个key的
     *
     * @param keys 多个key组成的集合
     * @return 多个key对应的value
     */
    public <T> List<T> mGetCacheObject(Collection<String> keys) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.multiGet(keys);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public boolean deleteObject(final String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    public long deleteObject(final Collection collection) {
        return redisTemplate.delete(collection);
    }

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> long setCacheSet(final String key, final Set<T> dataSet) {
        Long count = redisTemplate.opsForSet().add(key, dataSet);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    public <T> void delCacheMapValue(final String key, final String hKey) {
        redisTemplate.opsForHash().delete(key, hKey);
    }

    public long incrByCacheMapValue(final String key, final String hKey, long value, Integer timeout) {
        Long increment = redisTemplate.opsForHash().increment(key, hKey, value);
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
        return increment;
    }

    /**
     * 获取Hash中的数据
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 缓存zset
     *
     * @param key   缓存键名
     * @param value 缓存键值
     * @param score 分数
     * @return 缓存数据的对象
     */
    public <T> ZSetOperations<String, T> setCacheZset(String key, T value, double score) {
        ZSetOperations operations = redisTemplate.opsForZSet();
        operations.add(key, value, score);
        return operations;
    }

    /**
     * 删除zset
     *
     * @param key   缓存键名
     * @param value 缓存键值
     * @return 删除个数
     */
    public <T> Long deleteZsetObject(String key, T value) {
        ZSetOperations operations = redisTemplate.opsForZSet();
        return operations.remove(key, value);
    }

    /**
     * 获得缓存的set
     *
     * @param key 缓存键名
     * @param min 最低分数
     * @param max 最高分数
     * @return 满足分数区间的键值
     */
    public <T> Set<T> getCacheZset(String key, double min, double max) {
        ZSetOperations operations = redisTemplate.opsForZSet();
        return operations.rangeByScore(key, min, max);
    }

    /**
     * @param key
     * @param orderSnIncrLimit
     * @return
     */
    public Long luaIncrKey(String key, Integer orderSnIncrLimit) {
        RedisScript<Long> redisScript = new DefaultRedisScript<>(buildLuaIncrKeyScript(), Long.class);
        return (Long) redisTemplate.execute(redisScript, Collections.singletonList(key), orderSnIncrLimit);
    }

    /**
     * lua原子脚本
     */
    public static String buildLuaIncrKeyScript() {
        return """
                local key = KEYS[1]
                local limit = ARGV[1]
                local c = redis.call('get', key)
                if c and tonumber(c) > tonumber(limit) then
                    redis.call('set', key, 0)
                    return c
                end
                return redis.call('incr', key)
                """;
    }

}
