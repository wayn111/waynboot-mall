package com.wayn.mobile.design.strategy.concretestrategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Diamond;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.service.shop.IColumnGoodsRelationService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.mobile.design.strategy.JumpTypeEnum;
import com.wayn.mobile.design.strategy.strategy.DiamondJumpType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 栏目跳转策略
 */
@Component
@AllArgsConstructor
public class ColumnStrategy implements DiamondJumpType {

    private IColumnGoodsRelationService iColumnGoodsRelationService;

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
