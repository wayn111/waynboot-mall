package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.shop.*;
import com.wayn.common.core.domain.vo.GoodsSaveRelatedVO;
import com.wayn.common.core.domain.vo.SearchVO;
import com.wayn.common.core.mapper.shop.GoodsMapper;
import com.wayn.common.core.service.shop.*;
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
        if (Arrays.stream(products).filter(GoodsProduct::getDefaultSelected).count() > 1) {
            return R.error("商品规格只能选择一个启用默认选中");
        }

        // 保存商品规格
        iGoodsSpecificationService.saveBatch(Arrays.asList(specifications));
        // 保存商品属性
        iGoodsAttributeService.saveBatch(Arrays.asList(attributes));
        // 保存商品货品
        iGoodsProductService.saveBatch(Arrays.asList(products));
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
        return true;
    }

    @Override
    public R updateGoodsRelated(GoodsSaveRelatedVO goodsSaveRelatedVO) {
        Goods goods = goodsSaveRelatedVO.getGoods();
        GoodsAttribute[] attributes = goodsSaveRelatedVO.getAttributes();
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
        iGoodsAttributeService.updateBatchById(Arrays.asList(attributes));
        // 更新商品货品
        iGoodsProductService.updateBatchById(Arrays.asList(products));
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
