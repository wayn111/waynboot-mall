package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.base.entity.ElasticEntity;
import com.wayn.common.base.service.BaseElasticService;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.shop.*;
import com.wayn.common.core.domain.vo.GoodsSaveRelatedVO;
import com.wayn.common.core.domain.vo.SearchVO;
import com.wayn.common.core.mapper.shop.GoodsMapper;
import com.wayn.common.core.service.shop.*;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

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

    @Autowired
    private BaseElasticService baseElasticService;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public R saveGoodsRelated(GoodsSaveRelatedVO goodsSaveRelatedVO) {
        Goods goods = goodsSaveRelatedVO.getGoods();
        GoodsAttribute[] attributes = goodsSaveRelatedVO.getAttributes();
        GoodsSpecification[] specifications = goodsSaveRelatedVO.getSpecifications();
        GoodsProduct[] products = goodsSaveRelatedVO.getProducts();
        if (SysConstants.NOT_UNIQUE.equals(checkGoodsNameUnique(goods))) {
            return R.error("添加商品'" + goods.getName() + "'失败，商品名称已存在");
        }
        // 商品表里面有一个字段retailPrice记录当前商品的最低价
        BigDecimal retailPrice = new BigDecimal(Integer.MAX_VALUE);
        for (GoodsProduct product : products) {
            BigDecimal productPrice = product.getPrice();
            if (retailPrice.compareTo(productPrice) > 0) {
                retailPrice = productPrice;
            }
        }
        // 保存商品
        goods.setRetailPrice(retailPrice);
        goods.setCreateTime(new Date());
        save(goods);
        goods.setGoodsSn(goods.getId().toString());
        updateById(goods);
        for (GoodsSpecification specification : specifications) {
            specification.setGoodsId(goods.getId());
            specification.setCreateTime(new Date());
        }
        for (GoodsAttribute goodsAttribute : attributes) {
            goodsAttribute.setGoodsId(goods.getId());
            goodsAttribute.setCreateTime(new Date());
        }
        for (GoodsProduct goodsProduct : products) {
            goodsProduct.setGoodsId(goods.getId());
            goodsProduct.setCreateTime(new Date());
        }
        // 判断启用默认选中的规格是否超过一个
        if (Arrays.stream(products).filter(goodsProduct -> {
            if (goodsProduct.getDefaultSelected() == null) {
                return false;
            }
            return goodsProduct.getDefaultSelected();
        }).count() > 1) {
            return R.error("商品规格只能选择一个启用默认选中");
        }

        // 保存商品规格
        iGoodsSpecificationService.saveBatch(Arrays.asList(specifications));
        // 保存商品属性
        iGoodsAttributeService.saveBatch(Arrays.asList(attributes));
        // 保存商品货品
        iGoodsProductService.saveBatch(Arrays.asList(products));

        // 同步es
        ElasticEntity elasticEntity = new ElasticEntity();
        elasticEntity.setId(goods.getId().toString());
        Map<String, Object> map = new HashMap<>();
        map.put("id", goods.getId());
        map.put("name", goods.getName());
        map.put("countPrice", goods.getCounterPrice());
        map.put("retailPrice", goods.getRetailPrice());
        map.put("keyword", goods.getKeywords().split(","));
        map.put("isOnSale", goods.getIsOnSale());
        elasticEntity.setData(map);
        boolean one = baseElasticService.insertOrUpdateOne(SysConstants.ES_GOODS_INDEX, elasticEntity);
        if (!one) {
            throw new BusinessException("创建商品，同步es失败");
        }
        return R.success();
    }

    @Override
    public String checkGoodsNameUnique(Goods goods) {
        long goodsId = Objects.isNull(goods.getId()) ? -1L : goods.getId();
        Goods shopGoods = getOne(new QueryWrapper<Goods>().eq("name", goods.getName()));
        if (shopGoods != null && shopGoods.getId() != goodsId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteGoodsRelatedByGoodsId(Long goodsId) {
        removeById(goodsId);
        iGoodsSpecificationService.remove(new QueryWrapper<GoodsSpecification>().eq("goods_id", goodsId));
        iGoodsAttributeService.remove(new QueryWrapper<GoodsAttribute>().eq("goods_id", goodsId));
        iGoodsProductService.remove(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        // 同步es
        boolean one = baseElasticService.delete(SysConstants.ES_GOODS_INDEX, goodsId.toString());
        if (!one) {
            throw new BusinessException("删除商品，同步es失败");
        }
        return true;
    }

    @Override
    public R updateGoodsRelated(GoodsSaveRelatedVO goodsSaveRelatedVO) {
        Goods goods = goodsSaveRelatedVO.getGoods();
        GoodsAttribute[] attributes = goodsSaveRelatedVO.getAttributes();
        List<GoodsAttribute> updateAttributes = new ArrayList<>();
        List<GoodsAttribute> insertAttributes = new ArrayList<>();
        GoodsSpecification[] specifications = goodsSaveRelatedVO.getSpecifications();
        GoodsProduct[] products = goodsSaveRelatedVO.getProducts();
        if (SysConstants.NOT_UNIQUE.equals(checkGoodsNameUnique(goods))) {
            return R.error("更新商品'" + goods.getName() + "'失败，商品名称已存在");
        }
        // 商品表里面有一个字段retailPrice记录当前商品的最低价
        BigDecimal retailPrice = new BigDecimal(Integer.MAX_VALUE);
        for (GoodsProduct product : products) {
            BigDecimal productPrice = product.getPrice();
            if (retailPrice.compareTo(productPrice) > 0) {
                retailPrice = productPrice;
            }
        }
        // 保存商品
        goods.setRetailPrice(retailPrice);
        goods.setUpdateTime(new Date());
        updateById(goods);
        for (GoodsSpecification specification : specifications) {
            specification.setUpdateTime(new Date());
        }

        for (GoodsAttribute goodsAttribute : attributes) {
            goodsAttribute.setUpdateTime(new Date());
            if (goodsAttribute.getId() != null) {
                updateAttributes.add(goodsAttribute);
            } else {
                goodsAttribute.setGoodsId(goods.getId());
                insertAttributes.add(goodsAttribute);
            }
        }

        for (GoodsProduct goodsProduct : products) {
            goodsProduct.setUpdateTime(new Date());
        }
        // 判断启用默认选中的规格是否超过一个
        if (Arrays.stream(products).filter(GoodsProduct::getDefaultSelected).count() > 1) {
            return R.error("商品规格只能选择一个启用默认选中");
        }

        // 更新商品规格
        iGoodsSpecificationService.updateBatchById(Arrays.asList(specifications));
        // 更新商品属性
        iGoodsAttributeService.updateBatchById(updateAttributes);
        // 添加商品属性
        iGoodsAttributeService.saveBatch(insertAttributes);
        // 更新商品货品
        iGoodsProductService.updateBatchById(Arrays.asList(products));
        // 同步es
        ElasticEntity elasticEntity = new ElasticEntity();
        elasticEntity.setId(goods.getId().toString());
        Map<String, Object> map = new HashMap<>();
        map.put("id", goods.getId());
        map.put("name", goods.getName());
        map.put("countPrice", goods.getCounterPrice());
        map.put("retailPrice", goods.getRetailPrice());
        map.put("keyword", Objects.isNull(goods.getKeywords()) ? Collections.emptyList() : goods.getKeywords().split(","));
        map.put("isOnSale", goods.getIsOnSale());
        elasticEntity.setData(map);
        boolean one = baseElasticService.insertOrUpdateOne(SysConstants.ES_GOODS_INDEX, elasticEntity);
        if (!one) {
            throw new BusinessException("创建商品，同步es失败");
        }
        return R.success();
    }

    @Override
    public R selectListPageByCateIds(Page<Goods> page, List<Long> l2cateList) {
        return R.success().add("goods", goodsMapper.selectGoodsListPageByl2CateId(page, l2cateList).getRecords());
    }

    @Override
    public List<Goods> searchResult(Page<SearchVO> page, SearchVO searchVO) {
        return goodsMapper.searchResult(page, searchVO);
    }
}
