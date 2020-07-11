package com.wayn.admin.api.service.shop.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.admin.api.domain.shop.*;
import com.wayn.admin.api.mapper.shop.GoodsMapper;
import com.wayn.admin.api.service.shop.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品基本信息表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-07-06
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private IGoodsProductService iGoodsProductService;

    @Autowired
    private IGoodsAttributeService iGoodsAttributeService;

    @Autowired
    private IGoodsSpecificationService iGoodsSpecificationService;

    @Autowired
    private ICategoryService iCategoryService;

    @Override
    public IPage<Goods> listPage(Page<Goods> page, Goods goods) {
        return goodsMapper.selectGoodsListPage(page, goods);
    }

    @Override
    public Map<String, Object> getGoodsInfoById(Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        List<GoodsProduct> goodsProducts = iGoodsProductService.list(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        List<GoodsAttribute> goodsAttributes = iGoodsAttributeService.list(new QueryWrapper<GoodsAttribute>().eq("goods_id", goodsId));
        List<GoodsSpecification> goodsSpecifications = iGoodsSpecificationService.list(new QueryWrapper<GoodsSpecification>().eq("goods_id", goodsId));
        Long categoryId = goods.getCategoryId();
        Category category = iCategoryService.getById(categoryId);
        List<Long> categoryIds = new ArrayList<>();
        if (category != null) {
            Long parentCategoryId = category.getPid();
            categoryIds.add(parentCategoryId);
            categoryIds.add(categoryId);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("goods", goods);
        data.put("specifications", goodsSpecifications);
        data.put("products", goodsProducts);
        data.put("attributes", goodsAttributes);
        data.put("categoryIds", categoryIds);
        return data;
    }
}
