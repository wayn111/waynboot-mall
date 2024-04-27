package com.wayn.common.core.vo;

import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.entity.shop.GoodsAttribute;
import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.entity.shop.GoodsSpecification;
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
