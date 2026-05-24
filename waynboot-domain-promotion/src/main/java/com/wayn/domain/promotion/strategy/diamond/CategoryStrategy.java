package com.wayn.domain.promotion.strategy.diamond;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.domain.api.promotion.entity.Diamond;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.mapper.GoodsMapper;
import com.wayn.domain.promotion.strategy.diamond.JumpTypeEnum;
import com.wayn.domain.promotion.strategy.diamond.DiamondJumpTypeInterface;
import com.wayn.common.model.response.DiamondGoodsResVO;
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
