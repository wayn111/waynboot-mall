package com.wayn.mobile.api.controller.goods;

import com.wayn.common.model.request.SearchRequestVO;
import com.wayn.common.model.response.SearchGoodsItemResVO;
import com.wayn.mobile.api.service.SearchApplicationService;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.constant.Constants;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchApplicationService searchApplicationService;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void resultShouldDelegateSearchServiceAndReturnVoList() throws Exception {
        SearchController controller = new SearchController(searchApplicationService);
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

        SearchGoodsItemResVO first = new SearchGoodsItemResVO();
        first.setId(2L);
        first.setName("高钙牛奶");
        SearchGoodsItemResVO second = new SearchGoodsItemResVO();
        second.setId(1L);
        second.setName("纯牛奶");
        when(searchApplicationService.searchResult(any(), any(), any())).thenReturn(List.of(first, second));

        try (MockedStatic<MobileSecurityUtils> mockedSecurity = mockStatic(MobileSecurityUtils.class)) {
            mockedSecurity.when(MobileSecurityUtils::getUserId).thenReturn(66L);

            R<List<SearchGoodsItemResVO>> result = controller.result(reqVO);

            assertEquals(ReturnCodeEnum.SUCCESS.getCode(), result.getCode());
            assertEquals(2, result.getData().size());
            assertEquals(2L, result.getData().get(0).getId());
            assertEquals("高钙牛奶", result.getData().get(0).getName());
            assertEquals(1L, result.getData().get(1).getId());
            assertEquals("纯牛奶", result.getData().get(1).getName());
            verify(searchApplicationService).searchResult(any(), any(), any());
        }
    }
}
