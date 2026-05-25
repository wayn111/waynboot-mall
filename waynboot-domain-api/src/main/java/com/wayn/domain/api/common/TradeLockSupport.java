package com.wayn.domain.api.common;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * 交易锁支撑接口。
 * 契约层只定义交易链路加锁语义，具体 Redis 锁实现由业务实现模块提供。
 */
public interface TradeLockSupport {

    /**
     * 在锁内执行无返回值动作。
     *
     * @param lockKey 锁 Key
     * @param timeout 锁超时时间
     * @param exceptionSupplier 加锁失败异常提供器
     * @param action 待执行动作
     */
    void runWithLock(String lockKey, Integer timeout, Supplier<? extends RuntimeException> exceptionSupplier,
                     Runnable action);

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
    <T> T executeWithLock(String lockKey, Integer timeout, Supplier<? extends RuntimeException> exceptionSupplier,
                          Supplier<T> supplier);

    /**
     * 按固定顺序获取多把锁后执行带返回值动作。
     *
     * @param lockKeys 锁 Key 集合
     * @param timeout 锁超时时间
     * @param exceptionSupplier 加锁失败异常提供器
     * @param supplier 待执行动作
     * @return 动作执行结果
     * @param <T> 返回值类型
     */
    <T> T executeWithLocks(Collection<String> lockKeys, Integer timeout,
                           Supplier<? extends RuntimeException> exceptionSupplier, Supplier<T> supplier);

    /**
     * 尝试在锁内执行动作。
     *
     * @param lockKey 锁 Key
     * @param timeout 锁超时时间
     * @param action 待执行动作
     * @return 是否执行成功
     */
    boolean tryRunWithLock(String lockKey, Integer timeout, Runnable action);
}
