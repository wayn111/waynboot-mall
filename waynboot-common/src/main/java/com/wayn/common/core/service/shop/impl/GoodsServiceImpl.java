package com.wayn.common.core.service.shop.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.entity.shop.Category;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.entity.shop.GoodsAttribute;
import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.entity.shop.GoodsSpecification;
import com.wayn.common.core.mapper.shop.GoodsMapper;
import com.wayn.common.core.service.shop.ICategoryService;
import com.wayn.common.core.service.shop.IGoodsAttributeService;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IGoodsSpecificationService;
import com.wayn.common.core.vo.GoodsAttributeVO;
import com.wayn.common.core.vo.GoodsProductVO;
import com.wayn.common.core.vo.GoodsSpecificationVO;
import com.wayn.common.core.vo.GoodsVO;
import com.wayn.common.model.request.GoodsSaveRelatedReqVO;
import com.wayn.common.model.response.GoodsManageDetailResVO;
import com.wayn.data.elastic.constant.EsConstants;
import com.wayn.data.elastic.manager.ElasticDocument;
import com.wayn.data.elastic.manager.ElasticEntity;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisLock;
import com.wayn.util.constant.SysConstants;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.file.FileUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 商品基本信息表服务实现
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
    private RedisLock redisLock;

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
    public GoodsManageDetailResVO getGoodsInfoById(Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
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
        GoodsManageDetailResVO data = new GoodsManageDetailResVO();
        data.setGoods(goods);
        data.setSpecifications(goodsSpecifications);
        data.setProducts(goodsProducts);
        data.setAttributes(goodsAttributes);
        data.setCategoryIds(categoryIds);
        data.setCategory(category);
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
        // 商品表中的 retailPrice 记录当前商品最低售价
        validateProducts(products);
        BigDecimal retailPrice = resolveRetailPrice(products);
        products.forEach(product -> product.setId(null));

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

        validateDefaultSelected(products);

        iGoodsSpecificationService.saveBatch(specifications);
        iGoodsAttributeService.saveBatch(attributes);
        iGoodsProductService.saveBatch(products);
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
        elasticDocument.delete(EsConstants.ES_GOODS_INDEX, goodsId.toString());
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
        validateProducts(products);
        BigDecimal retailPrice = resolveRetailPrice(products);

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

        validateDefaultSelected(products);

        iGoodsSpecificationService.updateBatchById(specifications);
        iGoodsAttributeService.updateBatchById(updateAttributes);
        iGoodsAttributeService.saveBatch(insertAttributes);
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

    @Override
    public void updateVirtualSales(Long goodsId, Integer number) {
        goodsMapper.updateVirtualSales(goodsId, number);
    }

    @Override
    public boolean syncGoodsToEs() {
        boolean lock = redisLock.lock(RedisKeyEnum.ES_SYNC_CACHE.getKey(), 2);
        if (!lock) {
            throw new BusinessException("加锁失败");
        }
        try {
            elasticDocument.deleteIndex(EsConstants.ES_GOODS_INDEX);
            try (InputStream inputStream = this.getClass().getResourceAsStream(EsConstants.ES_INDEX_GOODS_FILENAME)) {
                if (inputStream == null) {
                    throw new BusinessException("ES 索引配置不存在");
                }
                if (!elasticDocument.createIndex(EsConstants.ES_GOODS_INDEX, FileUtils.getContent(inputStream))) {
                    return false;
                }
            }
            List<ElasticEntity> entities = list().stream()
                    .map(this::buildGoodsElasticEntity)
                    .toList();
            return elasticDocument.insertBatch(EsConstants.ES_GOODS_INDEX, entities);
        } catch (IOException e) {
            throw new BusinessException("读取 ES 索引配置失败");
        } finally {
            redisLock.unLock(RedisKeyEnum.ES_SYNC_CACHE.getKey());
        }
    }

    /**
     * 同步单个商品到 ES
     *
     * @param goods 商品信息
     * @return 处理结果
     */
    public boolean syncGoods2Es(Goods goods) throws IOException {
        ElasticEntity elasticEntity = buildGoodsElasticEntity(goods);
        if (!elasticDocument.insertOrUpdateOne(EsConstants.ES_GOODS_INDEX, elasticEntity)) {
            throw new BusinessException("商品同步 ES 失败");
        }
        return true;
    }

    private void validateProducts(List<GoodsProduct> products) {
        if (products == null || products.isEmpty()) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
    }

    private BigDecimal resolveRetailPrice(List<GoodsProduct> products) {
        return products.stream()
                .map(GoodsProduct::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElseThrow(() -> new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR));
    }

    private void validateDefaultSelected(List<GoodsProduct> products) {
        long defaultSelectedCount = products.stream()
                .filter(product -> Boolean.TRUE.equals(product.getDefaultSelected()))
                .count();
        if (defaultSelectedCount > 1) {
            throw new BusinessException(ReturnCodeEnum.GOODS_SPEC_ONLY_START_ONE_DEFAULT_SELECTED_ERROR);
        }
    }

    private ElasticEntity buildGoodsElasticEntity(Goods goods) {
        ElasticEntity elasticEntity = new ElasticEntity();
        elasticEntity.setId(goods.getId().toString());
        Map<String, Object> map = new HashMap<>();
        map.put("id", goods.getId());
        map.put("name", goods.getName());
        map.put("pyname", goods.getName());
        map.put("sales", goods.getVirtualSales());
        map.put("isHot", goods.getIsHot());
        map.put("isNew", goods.getIsNew());
        map.put("countPrice", goods.getCounterPrice());
        map.put("retailPrice", goods.getRetailPrice());
        map.put("keyword", resolveKeywords(goods.getKeywords()));
        map.put("isOnSale", goods.getIsOnSale());
        map.put("createTime", goods.getCreateTime());
        elasticEntity.setData(map);
        return elasticEntity;
    }

    private List<String> resolveKeywords(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(keywords.split(","))
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .toList();
    }
}
