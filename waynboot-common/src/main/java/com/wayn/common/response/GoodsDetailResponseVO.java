package com.wayn.common.response;

import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.entity.shop.GoodsAttribute;
import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.vo.SpecificationVO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 商品详情返回
 */
@Data
public class GoodsDetailResponseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -4222874009558306929L;

    /**
     * 商品信息
     */
    private Goods info;
    /**
     * 规格列表
     */
    private List<SpecificationVO> specificationList;
    /**
     * 库存列表
     */
    private List<GoodsProduct> productList;
    /**
     * 属性列表
     */
    private List<GoodsAttribute> attributes;
}
