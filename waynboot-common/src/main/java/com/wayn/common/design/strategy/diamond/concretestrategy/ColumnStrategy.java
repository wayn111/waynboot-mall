package com.wayn.common.design.strategy.diamond.concretestrategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.entity.shop.Diamond;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.service.shop.IColumnGoodsRelationService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.design.strategy.diamond.JumpTypeEnum;
import com.wayn.common.design.strategy.diamond.strategy.DiamondJumpTypeInterface;
import com.wayn.common.response.DiamondGoodsResVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 栏目跳转策略
 */
@Component
@AllArgsConstructor
public class ColumnStrategy implements DiamondJumpTypeInterface {

    private IColumnGoodsRelationService iColumnGoodsRelationService;

    private IGoodsService iGoodsService;

    @Override
    public DiamondGoodsResVO getGoods(Page<Goods> page, Diamond diamond) {
        DiamondGoodsResVO resVO = new DiamondGoodsResVO();
        resVO.setGoods(iGoodsService.selectColumnGoodsPageByColumnId(page, diamond.getValueId()).getRecords());
        resVO.setDiamond(diamond);
        return resVO;
    }

    @Override
    public Integer getType() {
        return JumpTypeEnum.COLUMN.getType();
    }
}
