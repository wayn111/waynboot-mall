package com.wayn.admin.api.domain.vo;

import com.wayn.admin.api.domain.shop.Goods;
import com.wayn.admin.api.domain.shop.GoodsAttribute;
import com.wayn.admin.api.domain.shop.GoodsProduct;
import com.wayn.admin.api.domain.shop.GoodsSpecification;
import lombok.Data;

import javax.validation.Valid;

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
