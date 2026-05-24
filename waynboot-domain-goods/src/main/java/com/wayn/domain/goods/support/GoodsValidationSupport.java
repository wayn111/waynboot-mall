package com.wayn.domain.goods.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.goods.mapper.GoodsMapper;
import com.wayn.domain.api.goods.response.GoodsVO;
import com.wayn.util.constant.SysConstants;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 商品校验支撑服务。
 * 统一封装商品聚合写入前的名称唯一性、货品列表和默认 SKU 规则校验，
 * 避免商品保存、更新和同步流程各自维护一套规则。
 */
@Service
@AllArgsConstructor
public class GoodsValidationSupport {

    private final GoodsMapper goodsMapper;

    /**
     * 校验商品名称在当前商品维度下是否唯一。
     *
     * @param goods 商品基础信息
     * @return {@link SysConstants#UNIQUE} 或 {@link SysConstants#NOT_UNIQUE}
     */
    public String checkGoodsNameUnique(GoodsVO goods) {
        long goodsId = Objects.isNull(goods.getId()) ? -1L : goods.getId();
        Goods shopGoods = goodsMapper.selectOne(new QueryWrapper<Goods>().eq("name", goods.getName()));
        if (shopGoods != null && shopGoods.getId() != goodsId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    /**
     * 在商品保存或更新前强制校验商品名唯一性。
     *
     * @param goods 商品基础信息
     */
    public void ensureGoodsNameUnique(GoodsVO goods) {
        if (SysConstants.NOT_UNIQUE.equals(checkGoodsNameUnique(goods))) {
            throw new BusinessException(ReturnCodeEnum.CUSTOM_ERROR.setMsg(
                    String.format("%s商品[%s]失败，商品名称已存在", goods.getId() == null ? "添加" : "更新", goods.getName())));
        }
    }

    /**
     * 校验商品货品集合不能为空。
     *
     * @param products 商品货品集合
     */
    public void validateProducts(List<GoodsProduct> products) {
        if (products == null || products.isEmpty()) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
    }

    /**
     * 解析商品最低零售价。
     * 商品主表上的零售价始终与货品集合中的最低售价保持一致。
     *
     * @param products 商品货品集合
     * @return 最低零售价
     */
    public BigDecimal resolveRetailPrice(List<GoodsProduct> products) {
        validateProducts(products);
        return products.stream()
                .map(GoodsProduct::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElseThrow(() -> new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR));
    }

    /**
     * 校验默认 SKU 只能存在一个。
     *
     * @param products 商品货品集合
     */
    public void validateDefaultSelected(List<GoodsProduct> products) {
        validateProducts(products);
        long defaultSelectedCount = products.stream()
                .filter(product -> Boolean.TRUE.equals(product.getDefaultSelected()))
                .count();
        if (defaultSelectedCount > 1) {
            throw new BusinessException(ReturnCodeEnum.GOODS_SPEC_ONLY_START_ONE_DEFAULT_SELECTED_ERROR);
        }
    }
}
