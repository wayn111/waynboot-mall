package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.GoodsSpecification;

/**
 * 商品规格表 服务类
 *
 * @author wayn
 * @since 2020-07-06
 */
public interface IGoodsSpecificationService extends IService<GoodsSpecification> {


   Object  getSpecificationVOList(Long goodsId);
}
