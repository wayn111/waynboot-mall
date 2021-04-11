package com.wayn.mobile.api.service;

import com.wayn.common.util.R;

public interface IGoodsDetailService {

    /**
     * 获取商品详情，包含规格、产品库存、详情、评论等信息
     *
     * @param goodsId 商品ID
     * @return 获取商品详情
     */
    R getGoodsDetailData(Long goodsId);
}
