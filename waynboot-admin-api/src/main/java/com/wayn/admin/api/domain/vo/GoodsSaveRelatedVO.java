package com.wayn.admin.api.domain.vo;

import com.wayn.admin.api.domain.shop.Goods;
import com.wayn.admin.api.domain.shop.GoodsAttribute;
import com.wayn.admin.api.domain.shop.GoodsProduct;
import com.wayn.admin.api.domain.shop.GoodsSpecification;
import lombok.Data;

@Data
public class GoodsSaveRelatedVO {
    Goods goods;
    GoodsSpecification[] specifications;
    GoodsAttribute[] attributes;
    GoodsProduct[] products;
}
