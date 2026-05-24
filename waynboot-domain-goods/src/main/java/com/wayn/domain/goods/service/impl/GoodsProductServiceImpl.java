package com.wayn.domain.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.goods.mapper.GoodsProductMapper;
import com.wayn.domain.api.goods.service.IGoodsProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 商品货品表 服务实现类
 *
 * @author wayn
 * @since 2020-07-06
 */
@Service
@AllArgsConstructor
public class GoodsProductServiceImpl extends ServiceImpl<GoodsProductMapper, GoodsProduct> implements IGoodsProductService {

    private GoodsProductMapper goodsProductMapper;

    /**
     * 增加 SKU 可售库存。
     *
     * @param productId 商品货品ID
     * @param number    增加数量
     * @return true=更新成功
     */
    @Override
    public boolean addStock(Long productId, Integer number) {
        return goodsProductMapper.addStock(productId, number);
    }

    /**
     * 根据 SKU ID 批量查询货品。
     *
     * @param productIds 商品货品 ID 列表
     * @return 商品货品列表
     */
    @Override
    public List<GoodsProduct> selectProductByIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<GoodsProduct> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(GoodsProduct::getId, productIds.stream().distinct().toList());
        return list(queryWrapper);
    }

    /**
     * 减少 SKU 可售库存。
     *
     * @param productId 商品货品ID
     * @param number    减少数量
     * @return true=更新成功
     */
    @Override
    public boolean reduceStock(Long productId, Integer number) {
        return goodsProductMapper.reduceStock(productId, number);
    }

    /**
     * 冻结 SKU 库存。
     *
     * @param productId 商品货品ID
     * @param number    冻结数量
     * @return true=冻结成功
     */
    @Override
    public boolean freezeStock(Long productId, Integer number) {
        return goodsProductMapper.freezeStock(productId, number);
    }

    /**
     * 释放 SKU 冻结库存。
     *
     * @param productId 商品货品ID
     * @param number    释放数量
     * @return true=释放成功
     */
    @Override
    public boolean releaseFrozenStock(Long productId, Integer number) {
        return goodsProductMapper.releaseFrozenStock(productId, number);
    }

    /**
     * 确认 SKU 冻结库存。
     *
     * @param productId 商品货品ID
     * @param number    确认数量
     * @return true=确认成功
     */
    @Override
    public boolean confirmFrozenStock(Long productId, Integer number) {
        return goodsProductMapper.confirmFrozenStock(productId, number);
    }
}
