package com.wayn.domain.goods.support;

import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.entity.GoodsAttribute;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.goods.entity.GoodsSpecification;
import com.wayn.domain.api.goods.mapper.GoodsMapper;
import com.wayn.domain.api.goods.service.IGoodsAttributeService;
import com.wayn.domain.api.goods.service.IGoodsProductService;
import com.wayn.domain.api.goods.service.IGoodsSpecificationService;
import com.wayn.domain.api.goods.response.GoodsAttributeVO;
import com.wayn.domain.api.goods.response.GoodsProductVO;
import com.wayn.domain.api.goods.response.GoodsSpecificationVO;
import com.wayn.domain.api.goods.response.GoodsVO;
import com.wayn.domain.api.goods.request.GoodsSaveRelatedReqVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsMutationSupportTest {

    @Mock
    private GoodsMapper goodsMapper;
    @Mock
    private IGoodsProductService goodsProductService;
    @Mock
    private IGoodsAttributeService goodsAttributeService;
    @Mock
    private IGoodsSpecificationService goodsSpecificationService;
    @Mock
    private GoodsValidationSupport goodsValidationSupport;

    @Test
    void updateGoodsRelatedRemovesMissingChildrenAndSavesNewOnes() {
        GoodsMutationSupport support = new GoodsMutationSupport(goodsMapper, goodsProductService, goodsAttributeService,
                goodsSpecificationService, goodsValidationSupport);
        GoodsSaveRelatedReqVO reqVO = new GoodsSaveRelatedReqVO();
        GoodsVO goodsVO = new GoodsVO();
        goodsVO.setId(100L);
        goodsVO.setName("更新商品");
        reqVO.setGoods(goodsVO);
        GoodsSpecificationVO keepSpec = new GoodsSpecificationVO();
        keepSpec.setId(1L);
        keepSpec.setValue("L");
        GoodsSpecificationVO newSpec = new GoodsSpecificationVO();
        newSpec.setValue("XL");
        reqVO.setSpecifications(new GoodsSpecificationVO[]{keepSpec, newSpec});
        GoodsAttributeVO keepAttr = new GoodsAttributeVO();
        keepAttr.setId(10L);
        keepAttr.setAttribute("材质");
        GoodsAttributeVO newAttr = new GoodsAttributeVO();
        newAttr.setAttribute("产地");
        reqVO.setAttributes(new GoodsAttributeVO[]{keepAttr, newAttr});
        GoodsProductVO keepProduct = new GoodsProductVO();
        keepProduct.setId(1000L);
        keepProduct.setSpecifications(new String[]{"L"});
        keepProduct.setPrice(new BigDecimal("19.90"));
        keepProduct.setDefaultSelected(true);
        keepProduct.setNumber(10);
        GoodsProductVO newProduct = new GoodsProductVO();
        newProduct.setSpecifications(new String[]{"XL"});
        newProduct.setPrice(new BigDecimal("9.90"));
        newProduct.setDefaultSelected(false);
        newProduct.setNumber(8);
        reqVO.setProducts(new GoodsProductVO[]{keepProduct, newProduct});

        GoodsSpecification staleSpec = new GoodsSpecification();
        staleSpec.setId(2L);
        GoodsAttribute staleAttr = new GoodsAttribute();
        staleAttr.setId(20L);
        GoodsProduct staleProduct = new GoodsProduct();
        staleProduct.setId(2000L);

        when(goodsValidationSupport.resolveRetailPrice(any())).thenReturn(new BigDecimal("9.90"));
        when(goodsSpecificationService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<GoodsSpecification>>any()))
                .thenReturn(List.of(toExistingSpec(1L), staleSpec));
        when(goodsAttributeService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<GoodsAttribute>>any()))
                .thenReturn(List.of(toExistingAttr(10L), staleAttr));
        when(goodsProductService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<GoodsProduct>>any()))
                .thenReturn(List.of(toExistingProduct(1000L), staleProduct));

        support.updateGoodsRelated(reqVO);

        ArgumentCaptor<Goods> goodsCaptor = ArgumentCaptor.forClass(Goods.class);
        verify(goodsMapper).updateById(goodsCaptor.capture());
        assertEquals(new BigDecimal("9.90"), goodsCaptor.getValue().getRetailPrice());
        assertNotNull(goodsCaptor.getValue().getUpdateTime());

        verify(goodsSpecificationService).updateBatchById(any());
        verify(goodsSpecificationService).saveBatch(any());
        verify(goodsSpecificationService).removeByIds(eq(List.of(2L)));

        verify(goodsAttributeService).updateBatchById(any());
        verify(goodsAttributeService).saveBatch(any());
        verify(goodsAttributeService).removeByIds(eq(List.of(20L)));

        verify(goodsProductService).updateBatchById(any());
        verify(goodsProductService).saveBatch(any());
        verify(goodsProductService).removeByIds(eq(List.of(2000L)));
    }

    @Test
    void updateGoodsRelatedAdjustsExistingProductStockByDelta() {
        GoodsMutationSupport support = new GoodsMutationSupport(goodsMapper, goodsProductService, goodsAttributeService,
                goodsSpecificationService, goodsValidationSupport);
        GoodsSaveRelatedReqVO reqVO = new GoodsSaveRelatedReqVO();
        GoodsVO goodsVO = new GoodsVO();
        goodsVO.setId(100L);
        goodsVO.setName("库存调整商品");
        reqVO.setGoods(goodsVO);
        reqVO.setSpecifications(new GoodsSpecificationVO[0]);
        reqVO.setAttributes(new GoodsAttributeVO[0]);
        GoodsProductVO productVO = new GoodsProductVO();
        productVO.setId(1000L);
        productVO.setSpecifications(new String[]{"L"});
        productVO.setPrice(new BigDecimal("29.90"));
        productVO.setDefaultSelected(true);
        productVO.setNumber(6);
        reqVO.setProducts(new GoodsProductVO[]{productVO});

        GoodsProduct existingProduct = toExistingProduct(1000L);
        existingProduct.setNumber(10);

        when(goodsValidationSupport.resolveRetailPrice(any())).thenReturn(new BigDecimal("29.90"));
        when(goodsSpecificationService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<GoodsSpecification>>any()))
                .thenReturn(List.of());
        when(goodsAttributeService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<GoodsAttribute>>any()))
                .thenReturn(List.of());
        when(goodsProductService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<GoodsProduct>>any()))
                .thenReturn(List.of(existingProduct));
        when(goodsProductService.reduceStock(1000L, 4)).thenReturn(true);

        support.updateGoodsRelated(reqVO);

        ArgumentCaptor<Collection<GoodsProduct>> productCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(goodsProductService).updateBatchById(productCaptor.capture());
        GoodsProduct updatedProduct = productCaptor.getValue().iterator().next();
        assertNull(updatedProduct.getNumber());
        verify(goodsProductService).reduceStock(1000L, 4);
    }

    private GoodsSpecification toExistingSpec(Long id) {
        GoodsSpecification specification = new GoodsSpecification();
        specification.setId(id);
        return specification;
    }

    private GoodsAttribute toExistingAttr(Long id) {
        GoodsAttribute attribute = new GoodsAttribute();
        attribute.setId(id);
        return attribute;
    }

    private GoodsProduct toExistingProduct(Long id) {
        GoodsProduct product = new GoodsProduct();
        product.setId(id);
        product.setNumber(10);
        return product;
    }
}
