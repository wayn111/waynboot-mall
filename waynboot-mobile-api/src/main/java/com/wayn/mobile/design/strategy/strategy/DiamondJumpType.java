package com.wayn.mobile.design.strategy.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Diamond;
import com.wayn.common.core.domain.shop.Goods;

import java.util.List;

/**
 * 金刚位跳转策略接口
 */
public interface DiamondJumpType {

    List<Goods> getGoods(Page<Goods> page, Diamond diamond);

    Integer getType();
}
