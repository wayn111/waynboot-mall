package com.wayn.mobile.api.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.entity.SearchHistory;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.domain.api.goods.service.IKeywordService;
import com.wayn.domain.api.goods.service.ISearchHistoryService;
import com.wayn.common.model.request.SearchRequestVO;
import com.wayn.common.model.response.SearchGoodsItemResVO;
import com.wayn.data.elastic.constant.EsConstants;
import com.wayn.data.elastic.manager.ElasticDocument;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchApplicationServiceTest {

    @Test
    void searchResultReturnsVoListAndKeepsElasticSearchOrder() throws Exception {
        IGoodsService goodsService = mock(IGoodsService.class);
        ISearchHistoryService searchHistoryService = mock(ISearchHistoryService.class);
        IKeywordService keywordService = mock(IKeywordService.class);
        ElasticDocument elasticDocument = mock(ElasticDocument.class);
        ThreadPoolTaskExecutor taskExecutor = mock(ThreadPoolTaskExecutor.class);
        SearchApplicationService service = new SearchApplicationService(
                goodsService, searchHistoryService, keywordService, elasticDocument, taskExecutor);
        SearchRequestVO reqVO = new SearchRequestVO();
        reqVO.setKeyword("牛奶");
        reqVO.setFilterNew(false);
        reqVO.setFilterHot(false);
        reqVO.setIsNew(false);
        reqVO.setIsHot(false);
        reqVO.setIsPrice(false);
        reqVO.setIsSales(false);
        reqVO.setOrderBy("asc");
        Page<SearchRequestVO> page = new Page<>(1, 10);
        JSONObject first = new JSONObject();
        first.put("id", 2);
        JSONObject second = new JSONObject();
        second.put("id", 1);
        when(elasticDocument.searchResult(eq(EsConstants.ES_GOODS_INDEX), any(SearchSourceBuilder.class), eq(JSONObject.class)))
                .thenReturn(List.of(first, second));
        Goods goods1 = new Goods();
        goods1.setId(1L);
        goods1.setName("纯牛奶");
        goods1.setPicUrl("https://img.example.com/milk-1.png");
        goods1.setRetailPrice(new BigDecimal("12.30"));
        Goods goods2 = new Goods();
        goods2.setId(2L);
        goods2.setName("高钙牛奶");
        goods2.setPicUrl("https://img.example.com/milk-2.png");
        goods2.setRetailPrice(new BigDecimal("15.80"));
        when(goodsService.searchResult(List.of(2, 1))).thenReturn(List.of(goods1, goods2));
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        List<SearchGoodsItemResVO> result = service.searchResult(reqVO, page, 66L);

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals("高钙牛奶", result.get(0).getName());
        assertEquals(1L, result.get(1).getId());
        assertEquals("纯牛奶", result.get(1).getName());
        ArgumentCaptor<SearchHistory> historyCaptor = ArgumentCaptor.forClass(SearchHistory.class);
        verify(searchHistoryService).save(historyCaptor.capture());
        assertEquals(66L, historyCaptor.getValue().getUserId());
        assertEquals("牛奶", historyCaptor.getValue().getKeyword());
    }
}
