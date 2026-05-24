package com.wayn.mobile.api.controller.goods;

import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.trade.service.IHomeService;
import com.wayn.common.model.response.RecommonGoodsItemResVO;
import com.wayn.util.constant.Constants;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private IHomeService homeService;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void recommonGoodsListShouldReturnItemVoList() {
        HomeController controller = new HomeController(homeService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(Constants.PAGE_NUMBER, "1");
        request.addParameter(Constants.PAGE_SIZE, "2");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Goods goods = new Goods();
        goods.setId(1L);
        goods.setName("推荐商品");
        goods.setBrief("推荐简介");
        goods.setPicUrl("https://img.example.com/1.png");
        goods.setRetailPrice(new BigDecimal("19.90"));
        goods.setCounterPrice(new BigDecimal("29.90"));
        goods.setIsNew(true);
        goods.setIsHot(false);
        goods.setActualSales(10);
        goods.setVirtualSales(20);
        when(homeService.listGoodsPage(any())).thenReturn(List.of(goods));

        R<List<RecommonGoodsItemResVO>> result = controller.recommonGoodsList();

        assertEquals(ReturnCodeEnum.SUCCESS.getCode(), result.getCode());
        assertEquals(1, result.getData().size());
        RecommonGoodsItemResVO item = result.getData().get(0);
        assertEquals(goods.getId(), item.getId());
        assertEquals(goods.getName(), item.getName());
        assertEquals(goods.getRetailPrice(), item.getRetailPrice());
        assertEquals(goods.getCounterPrice(), item.getCounterPrice());
    }
}
