package com.wayn.common.design.strategy.diamond.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.entity.shop.Diamond;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.response.DiamondGoodsResVO;

/**
 * 金刚位跳转策略接口
 */
public interface DiamondJumpTypeInterface {

    DiamondGoodsResVO getGoods(Page<Goods> page, Diamond diamond);

    Integer getType();
}
