package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.common.core.domain.shop.ColumnGoodsRelation;

/**
 * 栏目商品关联表 Mapper 接口
 *
 * @author wayn
 * @since 2020-10-10
 */
public interface ColumnGoodsRelationMapper extends BaseMapper<ColumnGoodsRelation> {

    Integer getGoodsNum(Long columnId);
}
