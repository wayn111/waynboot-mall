package com.wayn.common.design.strategy.diamond.concretestrategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.entity.shop.Diamond;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.mapper.shop.GoodsMapper;
import com.wayn.common.design.strategy.diamond.JumpTypeEnum;
import com.wayn.common.design.strategy.diamond.strategy.DiamondJumpTypeInterface;
import com.wayn.common.response.DiamondGoodsResVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 分类跳转策略
 */
@Component
@AllArgsConstructor
public class CategoryStrategy implements DiamondJumpTypeInterface {

    private GoodsMapper goodsMapper;

    @Override
    public DiamondGoodsResVO getGoods(Page<Goods> page, Diamond diamond) {
        DiamondGoodsResVO resVO = new DiamondGoodsResVO();
        List<Long> cateList = List.of(diamond.getValueId());
        resVO.setGoods(goodsMapper.selectGoodsListPageByl2CateId(page, cateList).getRecords());
        resVO.setDiamond(diamond);
        return resVO;
    }

    @Override
    public Integer getType() {
        return JumpTypeEnum.CATEGORY.getType();
    }
}
