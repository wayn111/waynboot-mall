package com.wayn.domain.cart.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.goods.service.IGoodsProductService;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 购物车校验支撑服务。
 * 统一封装商品上架、货品归属、库存上限和默认货品选择规则。
 */
@Service
@AllArgsConstructor
public class CartValidationSupport {

    private final IGoodsService goodsService;
    private final IGoodsProductService goodsProductService;

    /**
     * 校验购物车请求的基础参数。
     *
     * @param goodsId 商品 ID
     * @param productId 货品 ID
     * @param number 数量
     */
    public void validateRequest(Long goodsId, Long productId, Integer number) {
        if (!ObjectUtils.allNotNull(goodsId, productId, number) || number <= 0) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
    }

    /**
     * 查询并校验商品必须存在且处于上架状态。
     *
     * @param goodsId 商品 ID
     * @return 商品信息
     */
    public Goods requireOnSaleGoods(Long goodsId) {
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
        if (!Boolean.TRUE.equals(goods.getIsOnSale())) {
            throw new BusinessException(ReturnCodeEnum.GOODS_HAS_OFFSHELF_ERROR);
        }
        return goods;
    }

    /**
     * 查询并校验货品必须存在且归属于指定商品。
     *
     * @param productId 货品 ID
     * @param goodsId 商品 ID
     * @return 货品信息
     */
    public GoodsProduct requireProduct(Long productId, Long goodsId) {
        GoodsProduct product = goodsProductService.getById(productId);
        if (product == null || !Objects.equals(product.getGoodsId(), goodsId)) {
            throw new BusinessException(ReturnCodeEnum.GOODS_STOCK_NOT_ENOUGH_ERROR);
        }
        return product;
    }

    /**
     * 校验货品库存是否足够。
     *
     * @param product 货品信息
     * @param requiredNumber 目标数量
     */
    public void ensureEnoughStock(GoodsProduct product, int requiredNumber) {
        if (product == null || product.getNumber() == null || requiredNumber > product.getNumber()) {
            throw new BusinessException(ReturnCodeEnum.GOODS_STOCK_NOT_ENOUGH_ERROR);
        }
    }

    /**
     * 解析商品默认货品。
     * 如果没有显式默认货品，则回退到第一个货品。
     *
     * @param goodsId 商品 ID
     * @return 默认货品
     */
    public GoodsProduct resolveDefaultProduct(Long goodsId) {
        List<GoodsProduct> products = goodsProductService.list(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        if (CollectionUtils.isEmpty(products)) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
        return products.stream()
                .filter(product -> Boolean.TRUE.equals(product.getDefaultSelected()))
                .findFirst()
                .orElse(products.get(0));
    }
}
