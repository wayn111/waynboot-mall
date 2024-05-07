package com.wayn.common.core.service.shop.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.entity.shop.*;
import com.wayn.common.core.mapper.shop.GoodsMapper;
import com.wayn.common.core.service.shop.*;
import com.wayn.common.core.vo.GoodsAttributeVO;
import com.wayn.common.core.vo.GoodsProductVO;
import com.wayn.common.core.vo.GoodsSpecificationVO;
import com.wayn.common.core.vo.GoodsVO;
import com.wayn.common.request.GoodsSaveRelatedReqVO;
import com.wayn.data.elastic.constant.EsConstants;
import com.wayn.data.elastic.manager.ElasticDocument;
import com.wayn.data.elastic.manager.ElasticEntity;
import com.wayn.util.constant.SysConstants;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * 商品基本信息表 服务实现类
 *
 * @author wayn
 * @since 2020-07-06
 */
@Service
@AllArgsConstructor
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    private GoodsMapper goodsMapper;
    private IGoodsProductService iGoodsProductService;
    private IGoodsAttributeService iGoodsAttributeService;
    private IGoodsSpecificationService iGoodsSpecificationService;
    private ICategoryService iCategoryService;
    private ElasticDocument elasticDocument;

    @Override
    public IPage<Goods> listPage(Page<Goods> page, Goods goods) {
        return goodsMapper.selectGoodsListPage(page, goods);
    }


    @Override
    public List<Goods> selectHomeIndexGoods(Goods goods) {
        return goodsMapper.selectHomeIndex(goods);
    }


    @Override
    public IPage<Goods> listColumnBindGoodsPage(Page<Goods> page, Goods goods, List<Long> columnGoodsIds) {
        return goodsMapper.selectColumnBindGoodsListPage(page, goods, columnGoodsIds);
    }

    @Override
    public IPage<Goods> listColumnUnBindGoodsPage(Page<Goods> page, Goods goods, List<Long> columnGoodsIds) {
        return goodsMapper.selectColumnUnBindGoodsListPage(page, goods, columnGoodsIds);
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
    public void saveGoodsRelated(GoodsSaveRelatedReqVO goodsSaveRelatedReqVO) {
        GoodsVO goodsVO = goodsSaveRelatedReqVO.getGoods();
        GoodsAttributeVO[] attributesVO = goodsSaveRelatedReqVO.getAttributes();
        GoodsSpecificationVO[] specificationsVO = goodsSaveRelatedReqVO.getSpecifications();
        GoodsProductVO[] productsVO = goodsSaveRelatedReqVO.getProducts();
        if (SysConstants.NOT_UNIQUE.equals(checkGoodsNameUnique(goodsVO))) {
            throw new BusinessException(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("添加商品[%s]失败，商品名称已存在", goodsVO.getName())));
        }
        List<GoodsSpecification> specifications = BeanUtil.copyToList(Arrays.asList(specificationsVO), GoodsSpecification.class);
        List<GoodsAttribute> attributes = BeanUtil.copyToList(Arrays.asList(attributesVO), GoodsAttribute.class);
        List<GoodsProduct> products = BeanUtil.copyToList(Arrays.asList(productsVO), GoodsProduct.class);
        // 商品表里面有一个字段retailPrice记录当前商品的最低价
        BigDecimal retailPrice = new BigDecimal(Integer.MAX_VALUE);
        for (GoodsProduct product : products) {
            BigDecimal productPrice = product.getPrice();
            if (retailPrice.compareTo(productPrice) > 0) {
                retailPrice = productPrice;
            }
        }
        // 保存商品
        Goods goods = BeanUtil.copyProperties(goodsVO, Goods.class);
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
        if (products.stream().filter(goodsProduct -> {
            if (goodsProduct.getDefaultSelected() == null) {
                return false;
            }
            return goodsProduct.getDefaultSelected();
        }).count() > 1) {
            throw new BusinessException(ReturnCodeEnum.GOODS_SPEC_ONLY_START_ONE_DEFAULT_SELECTED_ERROR);
        }

        // 保存商品规格
        iGoodsSpecificationService.saveBatch(specifications);
        // 保存商品属性
        iGoodsAttributeService.saveBatch(attributes);
        // 保存商品货品
        iGoodsProductService.saveBatch(products);

        // baseElasticService.syncGoods2Es(goods);
    }

    @Override
    public String checkGoodsNameUnique(GoodsVO goods) {
        long goodsId = Objects.isNull(goods.getId()) ? -1L : goods.getId();
        Goods shopGoods = getOne(new QueryWrapper<Goods>().eq("name", goods.getName()));
        if (shopGoods != null && shopGoods.getId() != goodsId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteGoodsRelatedByGoodsId(Long goodsId) throws IOException {
        removeById(goodsId);
        iGoodsSpecificationService.remove(new QueryWrapper<GoodsSpecification>().eq("goods_id", goodsId));
        iGoodsAttributeService.remove(new QueryWrapper<GoodsAttribute>().eq("goods_id", goodsId));
        iGoodsProductService.remove(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        // 同步es
        boolean one = elasticDocument.delete(EsConstants.ES_GOODS_INDEX, goodsId.toString());
        if (!one) {
            throw new BusinessException("删除商品，同步es失败");
        }
        return true;
    }

    @Override
    public void updateGoodsRelated(GoodsSaveRelatedReqVO goodsSaveRelatedReqVO) {
        GoodsVO goodsVO = goodsSaveRelatedReqVO.getGoods();
        GoodsAttributeVO[] attributesVO = goodsSaveRelatedReqVO.getAttributes();
        GoodsSpecificationVO[] specificationsVO = goodsSaveRelatedReqVO.getSpecifications();
        GoodsProductVO[] productsVO = goodsSaveRelatedReqVO.getProducts();
        if (SysConstants.NOT_UNIQUE.equals(checkGoodsNameUnique(goodsVO))) {
            throw new BusinessException(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("更新商品[%s]失败，商品名称已存在", goodsVO.getName())));
        }
        List<GoodsSpecification> specifications = BeanUtil.copyToList(Arrays.asList(specificationsVO), GoodsSpecification.class);
        List<GoodsAttribute> attributes = BeanUtil.copyToList(Arrays.asList(attributesVO), GoodsAttribute.class);
        List<GoodsAttribute> updateAttributes = new ArrayList<>();
        List<GoodsAttribute> insertAttributes = new ArrayList<>();
        List<GoodsProduct> products = BeanUtil.copyToList(Arrays.asList(productsVO), GoodsProduct.class);
        // 商品表里面有一个字段retailPrice记录当前商品的最低价
        BigDecimal retailPrice = new BigDecimal(Integer.MAX_VALUE);
        for (GoodsProduct product : products) {
            BigDecimal productPrice = product.getPrice();
            if (retailPrice.compareTo(productPrice) > 0) {
                retailPrice = productPrice;
            }
        }
        // 保存商品
        Goods goods = BeanUtil.copyProperties(goodsVO, Goods.class);
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
        if (products.stream().filter(GoodsProduct::getDefaultSelected).count() > 1) {
            throw new BusinessException(ReturnCodeEnum.GOODS_SPEC_ONLY_START_ONE_DEFAULT_SELECTED_ERROR);
        }

        // 更新商品规格
        iGoodsSpecificationService.updateBatchById(specifications);
        // 更新商品属性
        iGoodsAttributeService.updateBatchById(updateAttributes);
        // 添加商品属性
        iGoodsAttributeService.saveBatch(insertAttributes);
        // 更新商品货品
        iGoodsProductService.updateBatchById(products);
    }

    @Override
    public List<Goods> selectListPageByCateIds(Page<Goods> page, List<Long> l2cateList) {
        return goodsMapper.selectGoodsListPageByl2CateId(page, l2cateList).getRecords();
    }

    @Override
    public List<Goods> searchResult(List<?> goodsIdList) {
        return goodsMapper.selectHomeGoodsListByIds(goodsIdList);
    }

    @Override
    public IPage<Goods> selectColumnGoodsPageByColumnId(Page<Goods> page, Long columnId) {
        return goodsMapper.selectColumnGoodsPage(page, columnId);
    }

    @Override
    public List<Goods> selectGoodsByIds(List<Long> goodsIdList) {
        return goodsMapper.selectGoodsListByIds(goodsIdList);
    }

    /**
     * 同步商品信息到es中
     *
     * @param goods 商品信息
     */
    public boolean syncGoods2Es(Goods goods) throws IOException {
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
        map.put("createTime", goods.getCreateTime());
        elasticEntity.setData(map);
        if (!elasticDocument.insertOrUpdateOne(EsConstants.ES_GOODS_INDEX, elasticEntity)) {
            throw new BusinessException("商品同步es失败");
        }
        return true;
    }
}
