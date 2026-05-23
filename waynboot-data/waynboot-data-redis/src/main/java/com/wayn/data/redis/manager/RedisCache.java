package com.wayn.data.redis.manager;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
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

    /**
     * lua原子递增脚本
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

    /**
     * 构建 Redis 库存预占脚本。
     * availableKey 保存热点 SKU 可售库存快照，reservedKey 保存当前正在进入 MySQL 条件冻结的并发预占量，orderKey 防止同一订单重复预占。
     *
     * @return Redis Lua 脚本
     */
    public static String buildLuaReserveStockScript() {
        return """
                local availableKey = KEYS[1]
                local reservedKey = KEYS[2]
                local orderKey = KEYS[3]
                local requestNumber = tonumber(ARGV[1])
                local ttlSeconds = tonumber(ARGV[2])
                if requestNumber == nil or requestNumber <= 0 then
                    return -3
                end
                local available = redis.call('get', availableKey)
                if not available then
                    return -2
                end
                if redis.call('exists', orderKey) == 1 then
                    return 1
                end
                local reserved = redis.call('get', reservedKey)
                if not reserved then
                    reserved = 0
                end
                if tonumber(available) - tonumber(reserved) < requestNumber then
                    return -1
                end
                redis.call('incrby', reservedKey, requestNumber)
                redis.call('set', orderKey, requestNumber, 'EX', ttlSeconds)
                redis.call('expire', reservedKey, ttlSeconds)
                return 1
                """;
    }

    /**
     * 构建 Redis 库存预占释放脚本。
     * 下单线程进入 MySQL 条件冻结后，无论成功还是失败都释放 in-flight 预占，避免 Redis 并发闸门长期占用。
     *
     * @return Redis Lua 脚本
     */
    public static String buildLuaReleaseReservedStockScript() {
        return """
                local reservedKey = KEYS[1]
                local orderKey = KEYS[2]
                local reservedNumber = redis.call('get', orderKey)
                if not reservedNumber then
                    return 0
                end
                local afterRelease = redis.call('decrby', reservedKey, tonumber(reservedNumber))
                if afterRelease < 0 then
                    redis.call('set', reservedKey, 0)
                end
                redis.call('del', orderKey)
                return 1
                """;
    }

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
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    public <T> void setCacheObject(final String key, final T value, final Integer timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 当 key 不存在时写入缓存对象。
     * 用于业务幂等和短时去重场景，依赖 Redis SET NX EX 保证并发下只有一个线程写入成功。
     *
     * @param key 缓存键值
     * @param value 缓存值
     * @param timeout 超时时间，单位秒
     * @param <T> 缓存值类型
     * @return true=写入成功；false=key 已存在或 Redis 返回失败
     */
    public <T> boolean setCacheObjectIfAbsent(final String key, final T value, final Integer timeout) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
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
     * 获取key剩余过期时间
     * 在 Redis 2.6 或更早版本中，如果键不存在或者键存在但没有关联的过期时间，则命令返回 -1。 <br>
     * 从 Redis 2.8 开始，发生错误时的返回值发生了变化：<br>
     * - 如果该键不存在，该命令将返回 -2。<br>
     * - 如果密钥存在但没有关联的过期时间，则该命令返回 -1。<br>
     * @param key redis key
     * @return long
     */
    public <T> Long ttl(final String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.getOperations().getExpire(key);
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
     * 使用 Lua 原子预占热点 SKU 库存并发闸门。
     *
     * @param availableKey Redis 可售库存快照 Key
     * @param reservedKey SKU 维度正在进入 MySQL 冻结链路的预占量 Key
     * @param orderKey 订单 SKU 维度预占幂等 Key
     * @param number 预占数量
     * @param ttlSeconds 预占过期秒数
     * @return 1=预占成功，-1=Redis 快照库存不足，-2=未初始化可售库存，-3=参数非法
     */
    public Long luaReserveStock(String availableKey, String reservedKey, String orderKey, Integer number,
                                Integer ttlSeconds) {
        RedisScript<Long> redisScript = new DefaultRedisScript<>(buildLuaReserveStockScript(), Long.class);
        return (Long) redisTemplate.execute(redisScript, List.of(availableKey, reservedKey, orderKey), number, ttlSeconds);
    }

    /**
     * 使用 Lua 原子释放热点 SKU 库存预占。
     *
     * @param reservedKey SKU 维度预占量 Key
     * @param orderKey 订单 SKU 维度预占幂等 Key
     * @param number 释放数量，脚本优先使用 orderKey 内保存的数量
     * @return 1=释放成功，0=预占不存在
     */
    public Long luaReleaseReservedStock(String reservedKey, String orderKey) {
        RedisScript<Long> redisScript = new DefaultRedisScript<>(buildLuaReleaseReservedStockScript(), Long.class);
        return (Long) redisTemplate.execute(redisScript, List.of(reservedKey, orderKey));
    }

    /**
     * 获取匹配的所有key，使用scan避免阻塞
     *
     * @param pattern 匹配keys的规则
     * @return 返回获取到的keys
     */
    public Set<String> scan(String pattern) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keysTmp = new HashSet<>();
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(ScanOptions.scanOptions()
                    .match(pattern)
                    .count(1000).build())) {
                while (cursor.hasNext()) {
                    keysTmp.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
            return keysTmp;
        });
    }

}
