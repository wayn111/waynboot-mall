package com.wayn.domain.goods.support;

import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.goods.mapper.GoodsMapper;
import com.wayn.domain.api.goods.response.GoodsVO;
import com.wayn.util.constant.SysConstants;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsValidationSupportTest {

    @Mock
    private GoodsMapper goodsMapper;

    @Test
    void checkGoodsNameUniqueReturnsNotUniqueWhenOtherGoodsUsesSameName() {
        GoodsValidationSupport support = new GoodsValidationSupport(goodsMapper);
        GoodsVO goodsVO = new GoodsVO();
        goodsVO.setId(1L);
        goodsVO.setName("测试商品");
        Goods existing = new Goods();
        existing.setId(2L);

        when(goodsMapper.selectOne(any())).thenReturn(existing);

        assertEquals(SysConstants.NOT_UNIQUE, support.checkGoodsNameUnique(goodsVO));
    }

    @Test
    void validateDefaultSelectedThrowsWhenMultipleProductsAreSelected() {
        GoodsValidationSupport support = new GoodsValidationSupport(goodsMapper);
        GoodsProduct first = new GoodsProduct();
        first.setDefaultSelected(true);
        GoodsProduct second = new GoodsProduct();
        second.setDefaultSelected(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> support.validateDefaultSelected(List.of(first, second)));

        assertEquals(ReturnCodeEnum.GOODS_SPEC_ONLY_START_ONE_DEFAULT_SELECTED_ERROR.getCode(), exception.getCode());
    }

    @Test
    void resolveRetailPriceReturnsLowestPrice() {
        GoodsValidationSupport support = new GoodsValidationSupport(goodsMapper);
        GoodsProduct first = new GoodsProduct();
        first.setPrice(new BigDecimal("15.80"));
        GoodsProduct second = new GoodsProduct();
        second.setPrice(new BigDecimal("9.90"));

        assertEquals(new BigDecimal("9.90"), support.resolveRetailPrice(List.of(first, second)));
    }
}
