package com.wayn.domain.goods.support;

import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.mapper.GoodsMapper;
import com.wayn.data.elastic.manager.ElasticDocument;
import com.wayn.data.redis.manager.RedisLock;
import com.wayn.util.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsElasticSyncSupportTest {

    @Mock
    private GoodsMapper goodsMapper;
    @Mock
    private ElasticDocument elasticDocument;
    @Mock
    private RedisLock redisLock;

    @Test
    void syncGoodsToEsThrowsWhenLockNotAcquired() throws Exception {
        GoodsElasticSyncSupport support = new GoodsElasticSyncSupport(goodsMapper, elasticDocument, redisLock);
        when(redisLock.lock(anyString(), eq(2))).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, support::syncGoodsToEs);

        assertEquals("加锁失败", exception.getMsg());
        verify(elasticDocument, never()).deleteIndex(anyString());
    }

    @Test
    void syncSingleGoodsToEsThrowsWhenInsertFails() throws Exception {
        GoodsElasticSyncSupport support = new GoodsElasticSyncSupport(goodsMapper, elasticDocument, redisLock);
        Goods goods = new Goods();
        goods.setId(1L);
        goods.setName("测试商品");
        when(elasticDocument.insertOrUpdateOne(anyString(), any())).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> support.syncGoods2Es(goods));

        assertEquals("商品同步 ES 失败", exception.getMsg());
    }
}
