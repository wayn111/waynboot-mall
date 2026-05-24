package com.wayn.domain.api.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.domain.api.goods.entity.GoodsSpecification;
import com.wayn.domain.api.goods.response.SpecificationVO;

import java.util.List;

/**
 * 商品规格表 服务类
 *
 * @author wayn
 * @since 2020-07-06
 */
public interface IGoodsSpecificationService extends IService<GoodsSpecification> {

    List<SpecificationVO> getSpecificationVOList(Long goodsId);
}
