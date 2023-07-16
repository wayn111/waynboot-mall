package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.ColumnGoodsRelation;
import com.wayn.common.core.mapper.shop.ColumnGoodsRelationMapper;
import com.wayn.common.core.service.shop.IColumnGoodsRelationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 栏目商品关联表 服务实现类
 *
 * @author wayn
 * @since 2020-10-10
 */
@Service
@AllArgsConstructor
public class ColumnGoodsRelationServiceImpl extends ServiceImpl<ColumnGoodsRelationMapper, ColumnGoodsRelation> implements IColumnGoodsRelationService {

    private ColumnGoodsRelationMapper columnGoodsRelationMapper;

    @Override
    public Integer getGoodsNum(Long columnId) {
        return columnGoodsRelationMapper.getGoodsNum(columnId);
    }
}
