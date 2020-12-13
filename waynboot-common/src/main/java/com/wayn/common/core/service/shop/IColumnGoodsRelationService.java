package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.ColumnGoodsRelation;

/**
 * <p>
 * 栏目商品关联表 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-10-10
 */
public interface IColumnGoodsRelationService extends IService<ColumnGoodsRelation> {

    /**
     * 获取栏目配置的商品数量
     * @param columnId 栏目ID
     * @return 商品数量
     */
    Integer getGoodsNum(Long columnId);
}
