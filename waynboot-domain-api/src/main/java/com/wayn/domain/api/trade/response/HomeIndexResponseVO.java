package com.wayn.domain.api.trade.response;

import com.wayn.domain.api.goods.entity.Banner;
import com.wayn.domain.api.promotion.entity.Diamond;
import com.wayn.domain.api.goods.entity.Goods;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 首页接口返回
 */
@Data
public class HomeIndexResponseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -14732478530341760L;

    /**
     * 轮播图列表
     */
    private List<Banner> bannerList;
    /**
     * 金刚区列表
     */
    private List<Diamond> diamondList;
    /**
     * 新品列表
     */
    private List<Goods> newGoodsList;
    /**
     * 热品列表
     */
    private List<Goods> hotGoodsList;
}
