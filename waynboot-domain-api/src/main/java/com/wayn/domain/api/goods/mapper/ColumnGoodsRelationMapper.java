package com.wayn.domain.api.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.domain.api.goods.entity.ColumnGoodsRelation;

/**
 * 栏目商品关联表 Mapper 接口
 *
 * @author wayn
 * @since 2020-10-10
 */
public interface ColumnGoodsRelationMapper extends BaseMapper<ColumnGoodsRelation> {

    Integer getGoodsNum(Long columnId);
}
