package com.wayn.domain.trade.support.common;

import com.wayn.data.redis.manager.RedisLock;
import com.wayn.domain.api.common.TradeLockSupport;
import com.wayn.util.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisTradeLockSupportTest {

    @Mock
    private RedisLock redisLock;

    @Test
    void executeWithLockRunsSupplierAndUnlocks() {
        TradeLockSupport tradeLockSupport = new RedisTradeLockSupport(redisLock);
        when(redisLock.lock("lock-key", 2)).thenReturn(true);

        String result = tradeLockSupport.executeWithLock("lock-key", 2,
                () -> new BusinessException("lock failed"),
                () -> "ok");

        assertEquals("ok", result);
        verify(redisLock).unLock("lock-key");
    }

    @Test
    void executeWithLockThrowsWhenLockFailed() {
        TradeLockSupport tradeLockSupport = new RedisTradeLockSupport(redisLock);
        when(redisLock.lock("lock-key", 2)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> tradeLockSupport.executeWithLock("lock-key", 2,
                        () -> new BusinessException("lock failed"),
                        () -> "ok"));

        assertEquals("lock failed", exception.getMsg());
    }

    @Test
    void executeWithLocksSortsLocksAndUnlocksInReverseOrder() {
        TradeLockSupport tradeLockSupport = new RedisTradeLockSupport(redisLock);
        when(redisLock.lock("a-lock")).thenReturn(true);
        when(redisLock.lock("b-lock")).thenReturn(true);

        String result = tradeLockSupport.executeWithLocks(List.of("b-lock", "a-lock", "a-lock"), null,
                () -> new BusinessException("lock failed"),
                () -> "ok");

        assertEquals("ok", result);
        InOrder inOrder = inOrder(redisLock);
        inOrder.verify(redisLock).lock("a-lock");
        inOrder.verify(redisLock).lock("b-lock");
        inOrder.verify(redisLock).unLock("b-lock");
        inOrder.verify(redisLock).unLock("a-lock");
    }
}
