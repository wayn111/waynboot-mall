package com.wayn.domain.trade.support.common;

import com.wayn.data.redis.manager.RedisLock;
import com.wayn.domain.api.common.TradeLockSupport;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;

/**
 * Redis 交易锁支撑实现。
 * 统一封装交易链路中的加锁、释放锁和失败处理逻辑，避免各个支撑服务重复书写样板代码。
 */
@Service
@AllArgsConstructor
public class RedisTradeLockSupport implements TradeLockSupport {

    private final RedisLock redisLock;

    /**
     * 在锁内执行无返回值动作。
     *
     * @param lockKey 锁 Key
     * @param timeout 锁超时时间
     * @param exceptionSupplier 加锁失败异常提供器
     * @param action 待执行动作
     */
    @Override
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
    @Override
    public <T> T executeWithLock(String lockKey, Integer timeout,
                                 Supplier<? extends RuntimeException> exceptionSupplier, Supplier<T> supplier) {
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
     * 按固定顺序获取多把锁后执行带返回值动作。
     * 交易批处理需要同时保护多个订单号，先排序去重可以降低多个消费者交叉获取锁时的死锁风险。
     *
     * @param lockKeys 锁 Key 集合
     * @param timeout 锁超时时间
     * @param exceptionSupplier 加锁失败异常提供器
     * @param supplier 待执行动作
     * @return 动作执行结果
     * @param <T> 返回值类型
     */
    @Override
    public <T> T executeWithLocks(Collection<String> lockKeys, Integer timeout,
                                  Supplier<? extends RuntimeException> exceptionSupplier, Supplier<T> supplier) {
        List<String> sortedLockKeys = new LinkedHashSet<>(lockKeys).stream()
                .sorted()
                .toList();
        List<String> lockedKeys = new ArrayList<>(sortedLockKeys.size());
        try {
            for (String lockKey : sortedLockKeys) {
                boolean locked = timeout == null ? redisLock.lock(lockKey) : redisLock.lock(lockKey, timeout);
                if (!locked) {
                    throw exceptionSupplier.get();
                }
                lockedKeys.add(lockKey);
            }
            return supplier.get();
        } finally {
            for (int i = lockedKeys.size() - 1; i >= 0; i--) {
                redisLock.unLock(lockedKeys.get(i));
            }
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
    @Override
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
