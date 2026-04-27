package com.wayn.common.core.service.shop.support;

import com.wayn.data.redis.manager.RedisLock;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * 交易锁支撑服务。
 * 统一封装交易链路中的加锁、释放锁和失败处理逻辑，避免各个支撑服务重复书写样板代码。
 */
@Service
@AllArgsConstructor
public class TradeLockSupport {

    private final RedisLock redisLock;

    /**
     * 在锁内执行无返回值动作。
     *
     * @param lockKey 锁 Key
     * @param timeout 锁超时时间
     * @param exceptionSupplier 加锁失败异常提供器
     * @param action 待执行动作
     */
    public void runWithLock(String lockKey, Integer timeout, Supplier<? extends RuntimeException> exceptionSupplier,
                            Runnable action) {
        executeWithLock(lockKey, timeout, exceptionSupplier, () -> {
            action.run();
            return null;
        });
    }

    /**
     * 在锁内执行带返回值动作。
     *
     * @param lockKey 锁 Key
     * @param timeout 锁超时时间
     * @param exceptionSupplier 加锁失败异常提供器
     * @param supplier 待执行动作
     * @return 动作执行结果
     * @param <T> 返回值类型
     */
    public <T> T executeWithLock(String lockKey, Integer timeout, Supplier<? extends RuntimeException> exceptionSupplier,
                                 Supplier<T> supplier) {
        boolean locked = timeout == null ? redisLock.lock(lockKey) : redisLock.lock(lockKey, timeout);
        if (!locked) {
            throw exceptionSupplier.get();
        }
        try {
            return supplier.get();
        } finally {
            redisLock.unLock(lockKey);
        }
    }

    /**
     * 尝试在锁内执行动作。
     *
     * @param lockKey 锁 Key
     * @param timeout 锁超时时间
     * @param action 待执行动作
     * @return 是否执行成功
     */
    public boolean tryRunWithLock(String lockKey, Integer timeout, Runnable action) {
        boolean locked = timeout == null ? redisLock.lock(lockKey) : redisLock.lock(lockKey, timeout);
        if (!locked) {
            return false;
        }
        try {
            action.run();
            return true;
        } finally {
            redisLock.unLock(lockKey);
        }
    }
}
