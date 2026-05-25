package com.wayn.domain.promotion.strategy.diamond;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.domain.api.promotion.entity.Diamond;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.service.IColumnGoodsRelationService;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.domain.api.promotion.response.DiamondGoodsResVO;
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
