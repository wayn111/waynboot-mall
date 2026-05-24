package com.wayn.mobile.api.controller.goods;

import com.wayn.domain.api.goods.entity.SearchHistory;
import com.wayn.domain.api.goods.service.ISearchHistoryService;
import com.wayn.common.model.request.SearchHistorySaveReqVO;
import com.wayn.common.model.response.SearchHistoryResVO;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SearchHistoryControllerTest {

    @Mock
    private ISearchHistoryService searchHistoryService;

    @Test
    void listShouldReturnSearchHistoryResVoList() {
        SearchHistoryController controller = new SearchHistoryController(searchHistoryService);
        SearchHistory history = new SearchHistory();
        history.setId(1L);
        history.setUserId(10L);
        history.setKeyword("奶粉");
        history.setFrom("app");
        history.setHasGoods(true);
        history.setCreateTime(LocalDateTime.now());
        when(searchHistoryService.selectList(10L)).thenReturn(List.of(history));

        try (MockedStatic<MobileSecurityUtils> mockedSecurity = mockStatic(MobileSecurityUtils.class)) {
            mockedSecurity.when(MobileSecurityUtils::getUserId).thenReturn(10L);

            R<List<SearchHistoryResVO>> result = controller.list();

            assertEquals(ReturnCodeEnum.SUCCESS.getCode(), result.getCode());
            assertEquals(1, result.getData().size());
            SearchHistoryResVO resVO = result.getData().get(0);
            assertEquals(history.getId(), resVO.getId());
            assertEquals(history.getKeyword(), resVO.getKeyword());
            assertEquals(history.getFrom(), resVO.getFrom());
            assertEquals(history.getHasGoods(), resVO.getHasGoods());
        }
    }

    @Test
    void addShouldMapSaveReqVoToEntity() {
        SearchHistoryController controller = new SearchHistoryController(searchHistoryService);
        SearchHistorySaveReqVO reqVO = new SearchHistorySaveReqVO();
        reqVO.setKeyword("营养品");
        reqVO.setFrom("h5");
        reqVO.setHasGoods(true);
        when(searchHistoryService.save(any(SearchHistory.class))).thenReturn(true);

        try (MockedStatic<MobileSecurityUtils> mockedSecurity = mockStatic(MobileSecurityUtils.class)) {
            mockedSecurity.when(MobileSecurityUtils::getUserId).thenReturn(11L);

            R<Boolean> result = controller.add(reqVO);

            ArgumentCaptor<SearchHistory> historyCaptor = ArgumentCaptor.forClass(SearchHistory.class);
            verify(searchHistoryService).save(historyCaptor.capture());
            SearchHistory saved = historyCaptor.getValue();
            assertEquals(11L, saved.getUserId());
            assertEquals(reqVO.getKeyword(), saved.getKeyword());
            assertEquals(reqVO.getFrom(), saved.getFrom());
            assertEquals(reqVO.getHasGoods(), saved.getHasGoods());
            assertEquals(ReturnCodeEnum.SUCCESS.getCode(), result.getCode());
        }
    }
}
