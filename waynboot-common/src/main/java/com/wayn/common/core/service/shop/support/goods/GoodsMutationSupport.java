package com.wayn.common.core.service.shop.support.goods;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.entity.shop.GoodsAttribute;
import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.entity.shop.GoodsSpecification;
import com.wayn.common.core.mapper.shop.GoodsMapper;
import com.wayn.common.core.service.shop.IGoodsAttributeService;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IGoodsSpecificationService;
import com.wayn.common.core.vo.GoodsAttributeVO;
import com.wayn.common.core.vo.GoodsProductVO;
import com.wayn.common.core.vo.GoodsSpecificationVO;
import com.wayn.common.core.vo.GoodsVO;
import com.wayn.common.model.request.GoodsSaveRelatedReqVO;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wayn.data.redis.constant.RedisKeyEnum.GOODS_DETAIL_CACHE;

/**
 * 商品写路径支撑服务。
 * 统一处理商品主表与规格、属性、货品等聚合数据的事务写入，并负责更新时的增删改对账。
 */
@Service
public class GoodsMutationSupport {

    private final GoodsMapper goodsMapper;
    private final IGoodsProductService goodsProductService;
    private final IGoodsAttributeService goodsAttributeService;
    private final IGoodsSpecificationService goodsSpecificationService;
    private final GoodsValidationSupport goodsValidationSupport;
    private final GoodsElasticSyncSupport goodsElasticSyncSupport;
    private final RedisCache redisCache;

    /**
     * Spring 运行时构造器。
     *
     * @param goodsMapper 商品主表 Mapper
     * @param goodsProductService 商品货品服务
     * @param goodsAttributeService 商品属性服务
     * @param goodsSpecificationService 商品规格服务
     * @param goodsValidationSupport 商品校验支撑服务
     * @param goodsElasticSyncSupport 商品 ES 同步支撑服务
     */
    @Autowired
    public GoodsMutationSupport(GoodsMapper goodsMapper, IGoodsProductService goodsProductService,
                                IGoodsAttributeService goodsAttributeService,
                                IGoodsSpecificationService goodsSpecificationService,
                                GoodsValidationSupport goodsValidationSupport,
                                GoodsElasticSyncSupport goodsElasticSyncSupport,
                                RedisCache redisCache) {
        this.goodsMapper = goodsMapper;
        this.goodsProductService = goodsProductService;
        this.goodsAttributeService = goodsAttributeService;
        this.goodsSpecificationService = goodsSpecificationService;
        this.goodsValidationSupport = goodsValidationSupport;
        this.goodsElasticSyncSupport = goodsElasticSyncSupport;
        this.redisCache = redisCache;
    }

    /**
     * 单元测试使用的精简构造器。
     * 当测试场景不关注 ES 删除联动时，可以不注入 ES 支撑服务。
     *
     * @param goodsMapper 商品主表 Mapper
     * @param goodsProductService 商品货品服务
     * @param goodsAttributeService 商品属性服务
     * @param goodsSpecificationService 商品规格服务
     * @param goodsValidationSupport 商品校验支撑服务
     */
    GoodsMutationSupport(GoodsMapper goodsMapper, IGoodsProductService goodsProductService,
                         IGoodsAttributeService goodsAttributeService,
                         IGoodsSpecificationService goodsSpecificationService,
                         GoodsValidationSupport goodsValidationSupport) {
        this(goodsMapper, goodsProductService, goodsAttributeService, goodsSpecificationService,
                goodsValidationSupport, null, null);
    }

    /**
     * 保存商品聚合数据。
     * 在同一事务中写入商品主表、规格、属性、货品，并把主表零售价收口为最低货品售价。
     *
     * @param goodsSaveRelatedReqVO 商品保存参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveGoodsRelated(GoodsSaveRelatedReqVO goodsSaveRelatedReqVO) {
        GoodsVO goodsVO = goodsSaveRelatedReqVO.getGoods();
        List<GoodsSpecification> specifications = copyArray(goodsSaveRelatedReqVO.getSpecifications(), GoodsSpecification.class);
        List<GoodsAttribute> attributes = copyArray(goodsSaveRelatedReqVO.getAttributes(), GoodsAttribute.class);
        List<GoodsProduct> products = copyArray(goodsSaveRelatedReqVO.getProducts(), GoodsProduct.class);

        goodsValidationSupport.ensureGoodsNameUnique(goodsVO);
        goodsValidationSupport.validateProducts(products);
        goodsValidationSupport.validateDefaultSelected(products);

        Date now = new Date();
        Goods goods = BeanUtil.copyProperties(goodsVO, Goods.class);
        goods.setRetailPrice(goodsValidationSupport.resolveRetailPrice(products));
        goods.setCreateTime(now);
        goodsMapper.insert(goods);
        goods.setGoodsSn(goods.getId().toString());
        goodsMapper.updateById(goods);

        specifications.forEach(specification -> {
            specification.setId(null);
            specification.setGoodsId(goods.getId());
            specification.setCreateTime(now);
        });
        attributes.forEach(attribute -> {
            attribute.setId(null);
            attribute.setGoodsId(goods.getId());
            attribute.setCreateTime(now);
        });
        products.forEach(product -> {
            product.setId(null);
            product.setGoodsId(goods.getId());
            product.setCreateTime(now);
        });
        saveBatchIfPresent(specifications, goodsSpecificationService::saveBatch);
        saveBatchIfPresent(attributes, goodsAttributeService::saveBatch);
        saveBatchIfPresent(products, goodsProductService::saveBatch);
        evictGoodsDetailCache(goods.getId());
    }

    /**
     * 删除商品及其关联聚合数据。
     *
     * @param goodsId 商品 ID
     * @return 删除结果
     * @throws IOException 删除 ES 文档时的 IO 异常
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGoodsRelatedByGoodsId(Long goodsId) throws IOException {
        goodsMapper.deleteById(goodsId);
        goodsSpecificationService.remove(new QueryWrapper<GoodsSpecification>().eq("goods_id", goodsId));
        goodsAttributeService.remove(new QueryWrapper<GoodsAttribute>().eq("goods_id", goodsId));
        goodsProductService.remove(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        if (goodsElasticSyncSupport != null) {
            goodsElasticSyncSupport.deleteGoodsFromEs(goodsId);
        }
        evictGoodsDetailCache(goodsId);
        return true;
    }

    /**
     * 更新商品聚合数据。
     * 该方法会对规格、属性、货品执行增删改对账，确保前端删除的子项在数据库中也被同步删除。
     *
     * @param goodsSaveRelatedReqVO 商品更新参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGoodsRelated(GoodsSaveRelatedReqVO goodsSaveRelatedReqVO) {
        GoodsVO goodsVO = goodsSaveRelatedReqVO.getGoods();
        List<GoodsSpecification> specifications = copyArray(goodsSaveRelatedReqVO.getSpecifications(), GoodsSpecification.class);
        List<GoodsAttribute> attributes = copyArray(goodsSaveRelatedReqVO.getAttributes(), GoodsAttribute.class);
        List<GoodsProduct> products = copyArray(goodsSaveRelatedReqVO.getProducts(), GoodsProduct.class);

        goodsValidationSupport.ensureGoodsNameUnique(goodsVO);
        goodsValidationSupport.validateProducts(products);
        goodsValidationSupport.validateDefaultSelected(products);

        Goods goods = BeanUtil.copyProperties(goodsVO, Goods.class);
        goods.setRetailPrice(goodsValidationSupport.resolveRetailPrice(products));
        goods.setUpdateTime(new Date());
        goodsMapper.updateById(goods);

        syncSpecifications(goods.getId(), specifications);
        syncAttributes(goods.getId(), attributes);
        syncProducts(goods.getId(), products);
        evictGoodsDetailCache(goods.getId());
    }

    /**
     * 更新商品虚拟销量。
     *
     * @param goodsId 商品 ID
     * @param number 增量销量
     */
    public void updateVirtualSales(Long goodsId, Integer number) {
        goodsMapper.updateVirtualSales(goodsId, number);
        evictGoodsDetailCache(goodsId);
    }

    /**
     * 清理商品详情缓存。
     * 商品详情缓存包含货品库存、规格和属性，任何商品聚合写入后都必须删除缓存，避免移动端读到旧库存或旧规格。
     *
     * @param goodsId 商品 ID
     */
    private void evictGoodsDetailCache(Long goodsId) {
        if (redisCache != null && goodsId != null) {
            redisCache.deleteObject(GOODS_DETAIL_CACHE.getKey(goodsId));
        }
    }

    /**
     * 对账并同步商品规格。
     *
     * @param goodsId 商品 ID
     * @param specifications 新规格集合
     */
    private void syncSpecifications(Long goodsId, List<GoodsSpecification> specifications) {
        List<GoodsSpecification> existing = goodsSpecificationService.list(new QueryWrapper<GoodsSpecification>().eq("goods_id", goodsId));
        List<GoodsSpecification> toUpdate = new ArrayList<>();
        List<GoodsSpecification> toInsert = new ArrayList<>();
        Date now = new Date();
        for (GoodsSpecification specification : specifications) {
            specification.setGoodsId(goodsId);
            specification.setUpdateTime(now);
            if (specification.getId() == null) {
                specification.setCreateTime(now);
                toInsert.add(specification);
            } else {
                toUpdate.add(specification);
            }
        }
        updateBatchIfPresent(toUpdate, goodsSpecificationService::updateBatchById);
        saveBatchIfPresent(toInsert, goodsSpecificationService::saveBatch);
        removeStaleEntities(existing, specifications, GoodsSpecification::getId, goodsSpecificationService::removeByIds);
    }

    /**
     * 对账并同步商品属性。
     *
     * @param goodsId 商品 ID
     * @param attributes 新属性集合
     */
    private void syncAttributes(Long goodsId, List<GoodsAttribute> attributes) {
        List<GoodsAttribute> existing = goodsAttributeService.list(new QueryWrapper<GoodsAttribute>().eq("goods_id", goodsId));
        List<GoodsAttribute> toUpdate = new ArrayList<>();
        List<GoodsAttribute> toInsert = new ArrayList<>();
        Date now = new Date();
        for (GoodsAttribute attribute : attributes) {
            attribute.setGoodsId(goodsId);
            attribute.setUpdateTime(now);
            if (attribute.getId() == null) {
                attribute.setCreateTime(now);
                toInsert.add(attribute);
            } else {
                toUpdate.add(attribute);
            }
        }
        updateBatchIfPresent(toUpdate, goodsAttributeService::updateBatchById);
        saveBatchIfPresent(toInsert, goodsAttributeService::saveBatch);
        removeStaleEntities(existing, attributes, GoodsAttribute::getId, goodsAttributeService::removeByIds);
    }

    /**
     * 对账并同步商品货品。
     *
     * @param goodsId 商品 ID
     * @param products 新货品集合
     */
    private void syncProducts(Long goodsId, List<GoodsProduct> products) {
        List<GoodsProduct> existing = goodsProductService.list(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        Map<Long, GoodsProduct> existingMap = existing.stream()
                .filter(product -> product.getId() != null)
                .collect(Collectors.toMap(GoodsProduct::getId, Function.identity()));
        List<GoodsProduct> toUpdate = new ArrayList<>();
        List<GoodsProduct> toInsert = new ArrayList<>();
        Date now = new Date();
        for (GoodsProduct product : products) {
            product.setGoodsId(goodsId);
            product.setUpdateTime(now);
            if (product.getId() == null) {
                product.setCreateTime(now);
                toInsert.add(product);
            } else {
                adjustExistingProductStock(product, existingMap.get(product.getId()));
                toUpdate.add(product);
            }
        }
        updateBatchIfPresent(toUpdate, goodsProductService::updateBatchById);
        saveBatchIfPresent(toInsert, goodsProductService::saveBatch);
        removeStaleEntities(existing, products, GoodsProduct::getId, goodsProductService::removeByIds);
    }

    /**
     * 对已有货品执行库存差量调整。
     * 管理端编辑商品时不能直接把页面提交的库存覆盖到数据库，否则会覆盖并发下单已经扣减的库存。
     * 因此已有 SKU 的库存只通过 addStock/reduceStock 做差量更新，其余字段再走普通批量更新。
     *
     * @param incoming 本次提交的货品
     * @param existing 数据库中当前货品
     */
    private void adjustExistingProductStock(GoodsProduct incoming, GoodsProduct existing) {
        if (existing == null || incoming.getNumber() == null || existing.getNumber() == null) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
        int stockDelta = incoming.getNumber() - existing.getNumber();
        incoming.setNumber(null);
        if (stockDelta == 0) {
            return;
        }
        boolean adjusted = stockDelta > 0
                ? goodsProductService.addStock(incoming.getId(), stockDelta)
                : goodsProductService.reduceStock(incoming.getId(), Math.abs(stockDelta));
        if (!adjusted) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "商品货品库存调整失败");
        }
    }

    /**
     * 删除本次提交中已经移除的旧子项。
     *
     * @param existing 数据库现有集合
     * @param incoming 本次提交的新集合
     * @param idGetter 子项 ID 提取器
     * @param removeAction 删除动作
     * @param <T> 子项类型
     */
    private <T> void removeStaleEntities(List<T> existing, List<T> incoming, Function<T, Long> idGetter,
                                         Function<List<Long>, Boolean> removeAction) {
        Set<Long> incomingIds = incoming.stream()
                .map(idGetter)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<Long> staleIds = existing.stream()
                .map(idGetter)
                .filter(Objects::nonNull)
                .filter(id -> !incomingIds.contains(id))
                .toList();
        if (!staleIds.isEmpty()) {
            removeAction.apply(staleIds);
        }
    }

    /**
     * 把前端 VO 数组转换成持久化实体列表。
     *
     * @param source 源数组
     * @param targetClass 目标类型
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的实体列表
     */
    private <S, T> List<T> copyArray(S[] source, Class<T> targetClass) {
        if (source == null || source.length == 0) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(Arrays.asList(source), targetClass);
    }

    /**
     * 在集合非空时执行批量更新。
     *
     * @param entities 待更新集合
     * @param updateAction 批量更新动作
     * @param <T> 实体类型
     */
    private <T> void updateBatchIfPresent(List<T> entities, Function<Collection<T>, Boolean> updateAction) {
        if (!entities.isEmpty()) {
            updateAction.apply(entities);
        }
    }

    /**
     * 在集合非空时执行批量保存。
     *
     * @param entities 待保存集合
     * @param saveAction 批量保存动作
     * @param <T> 实体类型
     */
    private <T> void saveBatchIfPresent(List<T> entities, Function<Collection<T>, Boolean> saveAction) {
        if (!entities.isEmpty()) {
            saveAction.apply(entities);
        }
    }
}
