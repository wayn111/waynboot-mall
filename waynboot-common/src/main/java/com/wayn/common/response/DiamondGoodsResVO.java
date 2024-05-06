package com.wayn.common.response;

import com.wayn.common.core.entity.shop.Diamond;
import com.wayn.common.core.entity.shop.Goods;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 金刚区跳转接口返回
 */
@Data
public class DiamondGoodsResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2451449889097496018L;

    /**
     * 金刚区详情
     */
    private Diamond diamond;

    /**
     * 跳转商品列表
     */
    private List<Goods> goods;

}
