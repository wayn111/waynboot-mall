package com.wayn.mobile.design.strategy.concretestrategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Diamond;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.mapper.shop.GoodsMapper;
import com.wayn.mobile.design.strategy.JumpTypeEnum;
import com.wayn.mobile.design.strategy.strategy.DiamondJumpType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 分类跳转策略
 */
@Component
public class CategoryStrategy implements DiamondJumpType {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public List<Goods> getGoods(Page<Goods> page, Diamond diamond) {
        List<Long> cateList = List.of(diamond.getValueId());
        return goodsMapper.selectGoodsListPageByl2CateId(page, cateList).getRecords();
    }

    @Override
    public Integer getType() {
        return JumpTypeEnum.CATEGORY.getType();
    }
}
