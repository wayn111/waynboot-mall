package com.wayn.mobile.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.entity.shop.SearchHistory;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IKeywordService;
import com.wayn.common.core.service.shop.ISearchHistoryService;
import com.wayn.common.model.request.SearchRequestVO;
import com.wayn.common.model.response.SearchGoodsItemResVO;
import com.wayn.data.elastic.constant.EsConstants;
import com.wayn.data.elastic.manager.ElasticDocument;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.constant.Constants;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private IGoodsService goodsService;
    @Mock
    private ISearchHistoryService searchHistoryService;
    @Mock
    private IKeywordService keywordService;
    @Mock
    private ElasticDocument elasticDocument;
    @Mock
    private ThreadPoolTaskExecutor taskExecutor;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void resultShouldReturnSearchGoodsItemVoListAndKeepEsOrder() throws Exception {
        SearchController controller = new SearchController(goodsService, searchHistoryService, keywordService, elasticDocument, taskExecutor);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(Constants.PAGE_NUMBER, "1");
        request.addParameter(Constants.PAGE_SIZE, "10");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        SearchRequestVO reqVO = new SearchRequestVO();
        reqVO.setKeyword("牛奶");
        reqVO.setFilterNew(false);
        reqVO.setFilterHot(false);
        reqVO.setIsNew(false);
        reqVO.setIsHot(false);
        reqVO.setIsPrice(false);
        reqVO.setIsSales(false);
        reqVO.setOrderBy("asc");

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

        try (MockedStatic<MobileSecurityUtils> mockedSecurity = mockStatic(MobileSecurityUtils.class)) {
            mockedSecurity.when(MobileSecurityUtils::getUserId).thenReturn(66L);

            R<List<SearchGoodsItemResVO>> result = controller.result(reqVO);

            assertEquals(ReturnCodeEnum.SUCCESS.getCode(), result.getCode());
            assertEquals(2, result.getData().size());
            assertEquals(2L, result.getData().get(0).getId());
            assertEquals("高钙牛奶", result.getData().get(0).getName());
            assertEquals(1L, result.getData().get(1).getId());
            assertEquals("纯牛奶", result.getData().get(1).getName());
            ArgumentCaptor<SearchHistory> historyCaptor = ArgumentCaptor.forClass(SearchHistory.class);
            verify(searchHistoryService).save(historyCaptor.capture());
            SearchHistory history = historyCaptor.getValue();
            assertEquals(66L, history.getUserId());
            assertEquals("牛奶", history.getKeyword());
            assertEquals(Boolean.TRUE, history.getHasGoods());
        }
    }
}
