package com.wayn.domain.api.promotion.response;

import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.promotion.entity.Diamond;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 金刚区跳转商品返回对象。
 * 作为营销领域对外 VO，放在 domain-api，避免 domain-promotion 依赖 common 大包。
 */
@Data
public class DiamondGoodsResVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2451449889097496018L;

    /**
     * 金刚区配置详情。
     */
    private Diamond diamond;

    /**
     * 跳转后的商品列表。
     */
    private List<Goods> goods;
}
