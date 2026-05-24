package com.wayn.domain.goods.service.impl;

import com.wayn.domain.api.goods.mapper.GoodsProductMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoodsProductServiceImplTest {

    /**
     * 验证冻结库存会委托 Mapper 执行 MySQL 条件更新。
     */
    @Test
    void freezeStockDelegatesConditionalUpdateToMapper() {
        GoodsProductMapper mapper = mock(GoodsProductMapper.class);
        GoodsProductServiceImpl service = new GoodsProductServiceImpl(mapper);
        when(mapper.freezeStock(100L, 3)).thenReturn(true);

        boolean result = service.freezeStock(100L, 3);

        assertTrue(result);
        verify(mapper).freezeStock(100L, 3);
    }

    /**
     * 验证释放冻结库存会委托 Mapper 原子回补可售库存。
     */
    @Test
    void releaseFrozenStockDelegatesConditionalUpdateToMapper() {
        GoodsProductMapper mapper = mock(GoodsProductMapper.class);
        GoodsProductServiceImpl service = new GoodsProductServiceImpl(mapper);
        when(mapper.releaseFrozenStock(100L, 3)).thenReturn(true);

        boolean result = service.releaseFrozenStock(100L, 3);

        assertTrue(result);
        verify(mapper).releaseFrozenStock(100L, 3);
    }

    /**
     * 验证确认冻结库存会委托 Mapper 原子扣减冻结库存。
     */
    @Test
    void confirmFrozenStockDelegatesConditionalUpdateToMapper() {
        GoodsProductMapper mapper = mock(GoodsProductMapper.class);
        GoodsProductServiceImpl service = new GoodsProductServiceImpl(mapper);
        when(mapper.confirmFrozenStock(100L, 3)).thenReturn(true);

        boolean result = service.confirmFrozenStock(100L, 3);

        assertTrue(result);
        verify(mapper).confirmFrozenStock(100L, 3);
    }
}
