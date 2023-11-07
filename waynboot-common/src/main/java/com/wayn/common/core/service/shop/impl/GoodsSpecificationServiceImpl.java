package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.GoodsSpecification;
import com.wayn.common.core.domain.shop.vo.SpecificationVO;
import com.wayn.common.core.mapper.shop.GoodsSpecificationMapper;
import com.wayn.common.core.service.shop.IGoodsSpecificationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品规格表 服务实现类
 *
 * @author wayn
 * @since 2020-07-06
 */
@Service
public class GoodsSpecificationServiceImpl extends ServiceImpl<GoodsSpecificationMapper, GoodsSpecification> implements IGoodsSpecificationService {

    @Override
    public List<SpecificationVO> getSpecificationVOList(Long goodsId) {
        List<GoodsSpecification> specificationList = list(new QueryWrapper<GoodsSpecification>().eq("goods_id", goodsId));
        Map<String, SpecificationVO> map = new HashMap<>();
        List<SpecificationVO> specificationVoList = new ArrayList<>();
        for (GoodsSpecification goodsSpecification : specificationList) {
            String specification = goodsSpecification.getSpecification();
            SpecificationVO goodsSpecificationVo = map.get(specification);
            if (goodsSpecificationVo == null) {
                goodsSpecificationVo = new SpecificationVO();
                goodsSpecificationVo.setName(specification);
                List<GoodsSpecification> valueList = new ArrayList<>();
                valueList.add(goodsSpecification);
                goodsSpecificationVo.setValueList(valueList);
                map.put(specification, goodsSpecificationVo);
                specificationVoList.add(goodsSpecificationVo);
            } else {
                List<GoodsSpecification> valueList = goodsSpecificationVo.getValueList();
                valueList.add(goodsSpecification);
            }
        }
        return specificationVoList;
    }

}
