package com.wayn.mobile.design.strategy.concretestrategy;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.ColumnGoodsRelation;
import com.wayn.common.core.domain.shop.Diamond;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.service.shop.IColumnGoodsRelationService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.mobile.design.strategy.JumpTypeEnum;
import com.wayn.mobile.design.strategy.strategy.DiamondJumpType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 栏目跳转策略
 */
@Component
public class ColumnStrategy implements DiamondJumpType {

    @Autowired
    private IColumnGoodsRelationService iColumnGoodsRelationService;

    @Autowired
    private IGoodsService iGoodsService;

    @Override
    public List<Goods> getGoods(Page<Goods> page, Diamond diamond) {
        List<ColumnGoodsRelation> goodsRelationList = iColumnGoodsRelationService.list(new QueryWrapper<ColumnGoodsRelation>()
                .eq("column_id", diamond.getValueId()));
        List<Long> goodsIdList = goodsRelationList.stream().map(ColumnGoodsRelation::getGoodsId).collect(Collectors.toList());
        Page<Goods> goodsPage = iGoodsService.page(page, new QueryWrapper<Goods>().in("id", goodsIdList).eq("is_on_sale", true));
        return goodsPage.getRecords();
    }

    @Override
    public Integer getType() {
        return JumpTypeEnum.COLUMN.getType();
    }
}
