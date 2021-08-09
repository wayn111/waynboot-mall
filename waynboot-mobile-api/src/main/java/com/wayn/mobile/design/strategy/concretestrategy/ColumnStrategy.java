package com.wayn.mobile.design.strategy.concretestrategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Diamond;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.service.shop.IColumnGoodsRelationService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.mobile.design.strategy.JumpTypeEnum;
import com.wayn.mobile.design.strategy.strategy.DiamondJumpType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
        return iGoodsService.selectColumnGoodsPageByColumnId(page, diamond.getValueId()).getRecords();
    }

    @Override
    public Integer getType() {
        return JumpTypeEnum.COLUMN.getType();
    }
}
