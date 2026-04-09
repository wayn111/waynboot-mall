package com.wayn.common.model.response;

import com.wayn.common.core.entity.shop.Category;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.entity.shop.GoodsAttribute;
import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.entity.shop.GoodsSpecification;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 商品管理详情返回
 */
@Data
public class GoodsManageDetailResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -9159218941817619296L;

    /**
     * 商品基础信息
     */
    private Goods goods;

    /**
     * 商品规格
     */
    private List<GoodsSpecification> specifications;

    /**
     * 商品货品
     */
    private List<GoodsProduct> products;

    /**
     * 商品属性
     */
    private List<GoodsAttribute> attributes;

    /**
     * 商品分类路径
     */
    private List<Long> categoryIds;

    /**
     * 商品所属分类
     */
    private Category category;
}
