package com.wayn.common.core.vo;

import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.entity.shop.GoodsAttribute;
import com.wayn.common.core.entity.shop.GoodsProduct;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author: waynaqua
 * @date: 2023/11/7 22:40
 */
@Data
public class GoodsDetailResponseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -4222874009558306929L;

    private Goods info;
    private List<SpecificationVO> specificationList;
    private List<GoodsProduct> productList;
    private List<GoodsAttribute> attributes;
}
