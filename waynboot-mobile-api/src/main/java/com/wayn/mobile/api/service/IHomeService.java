package com.wayn.mobile.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.util.R;

public interface IHomeService {

    /**
     * 获取首月数据（bannerList，category List，newGoodsList，hotGoodsList）
     * @return
     */
    R getHomeIndexData();

    R getGoodsList();

    R listGoodsPage(Page<Goods> page);
}
