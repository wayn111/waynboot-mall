package com.wayn.common.core.service.shop.support;

import com.wayn.data.redis.manager.RedisLock;
import com.wayn.util.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeLockSupportTest {

    @Mock
    private RedisLock redisLock;

    @Test
    void executeWithLockRunsSupplierAndUnlocks() {
        TradeLockSupport tradeLockSupport = new TradeLockSupport(redisLock);
        when(redisLock.lock("lock-key", 2)).thenReturn(true);

        String result = tradeLockSupport.executeWithLock("lock-key", 2,
                () -> new BusinessException("lock failed"),
                () -> "ok");

        assertEquals("ok", result);
        verify(redisLock).unLock("lock-key");
    }

    @Test
    void executeWithLockThrowsWhenLockFailed() {
        TradeLockSupport tradeLockSupport = new TradeLockSupport(redisLock);
        when(redisLock.lock("lock-key", 2)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> tradeLockSupport.executeWithLock("lock-key", 2,
                        () -> new BusinessException("lock failed"),
                        () -> "ok"));

        assertEquals("lock failed", exception.getMsg());
    }
}
