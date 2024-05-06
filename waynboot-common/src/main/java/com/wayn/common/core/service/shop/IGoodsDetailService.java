package com.wayn.common.core.service.shop;

import com.wayn.common.response.GoodsDetailResponseVO;

public interface IGoodsDetailService {

    /**
     * 获取商品详情，包含规格、产品库存、详情、评论等信息
     *
     * @param goodsId 商品ID
     * @return 获取商品详情
     */
    GoodsDetailResponseVO getGoodsDetailData(Long goodsId);
}
