package com.wayn.domain.api.goods.request;

import com.wayn.domain.api.goods.response.GoodsAttributeVO;
import com.wayn.domain.api.goods.response.GoodsProductVO;
import com.wayn.domain.api.goods.response.GoodsSpecificationVO;
import com.wayn.domain.api.goods.response.GoodsVO;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class GoodsSaveRelatedReqVO {

    @Valid
    GoodsVO goods;
    @Valid
    GoodsSpecificationVO[] specifications;
    @Valid
    GoodsAttributeVO[] attributes;
    @Valid
    GoodsProductVO[] products;
}
