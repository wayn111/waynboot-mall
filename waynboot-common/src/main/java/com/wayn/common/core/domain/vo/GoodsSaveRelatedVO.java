package com.wayn.common.core.domain.vo;

import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.shop.GoodsAttribute;
import com.wayn.common.core.domain.shop.GoodsProduct;
import com.wayn.common.core.domain.shop.GoodsSpecification;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class GoodsSaveRelatedVO {
    @Valid
    Goods goods;
    @Valid
    GoodsSpecification[] specifications;
    @Valid
    GoodsAttribute[] attributes;
    @Valid
    GoodsProduct[] products;
}
