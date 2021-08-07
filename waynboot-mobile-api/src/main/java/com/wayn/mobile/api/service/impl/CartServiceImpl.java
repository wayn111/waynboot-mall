package com.wayn.mobile.api.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.shop.GoodsProduct;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.util.R;
import com.wayn.common.util.bean.MyBeanUtil;
import com.wayn.mobile.api.domain.Cart;
import com.wayn.mobile.api.mapper.CartMapper;
import com.wayn.mobile.api.service.ICartService;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 购物车商品表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-08-03
 */
@Service
@AllArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    private IGoodsService iGoodsService;

    private IGoodsProductService iGoodsProductService;

    @Override
    public Cart checkExistsGoods(Long userId, Long goodsId, Long productId) {
        return getOne(new QueryWrapper<Cart>()
                .eq("user_id", userId)
                .eq("goods_id", goodsId)
                .eq("product_id", productId));
    }

    @Override
    public R add(Cart cart) {
        Long goodsId = cart.getGoodsId();
        Long productId = cart.getProductId();
        Integer number = cart.getNumber();
        if (!ObjectUtils.allNotNull(goodsId, productId, number) || number <= 0) {
            return R.error(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
        Goods goods = iGoodsService.getById(goodsId);
        if (!goods.getIsOnSale()) {
            return R.error(ReturnCodeEnum.GOODS_HAS_OFFSHELF_ERROR);
        }
        Long userId = MobileSecurityUtils.getLoginUser().getMember().getId();
        GoodsProduct product = iGoodsProductService.getById(productId);
        Cart existsCart = checkExistsGoods(userId, goodsId, productId);
        if (Objects.isNull(existsCart)) {
            if (Objects.isNull(product) || product.getNumber() < number) {
                return R.error(ReturnCodeEnum.GOODS_STOCK_NOT_ENOUGH_ERROR);
            }

            cart.setGoodsSn(goods.getGoodsSn());
            cart.setGoodsName(goods.getName());
            if (StringUtils.isEmpty(product.getUrl())) {
                cart.setPicUrl(goods.getPicUrl());
            } else {
                cart.setPicUrl(product.getUrl());
            }
            cart.setPrice(product.getPrice());
            cart.setSpecifications((product.getSpecifications()));
            cart.setUserId(Math.toIntExact(userId));
            cart.setChecked(true);
            cart.setRemark(goods.getBrief());
            cart.setCreateTime(LocalDateTime.now());
            save(cart);
        } else {
            int num = existsCart.getNumber() + number;
            if (num > product.getNumber()) {
                return R.error(ReturnCodeEnum.GOODS_STOCK_NOT_ENOUGH_ERROR);
            }
            existsCart.setNumber(num);
            cart.setUpdateTime(LocalDateTime.now());
            if (!updateById(existsCart)) {
                return R.error();
            }
        }
        return R.success();
    }

    @Override
    public R goodsCount() {
        Long userId = MobileSecurityUtils.getUserId();
        int count = count(new QueryWrapper<Cart>().eq("user_id", userId));
        return R.success().add("count", count);
    }

    @Override
    public R list(Long userId) {
        List<Cart> cartList = list(new QueryWrapper<Cart>().eq("user_id", userId));
        List<Long> goodsIdList = cartList.stream().map(Cart::getGoodsId).collect(Collectors.toList());
        Map<Long, Goods> goodsIdMap = iGoodsService
                .list(Wrappers.lambdaQuery(Goods.class)
                        .in(CollectionUtils.isNotEmpty(goodsIdList), Goods::getId, goodsIdList))
                .stream().collect(Collectors.toMap(Goods::getId, goods -> goods));

        JSONArray array = new JSONArray();
        for (Cart cart : cartList) {
            JSONObject jsonObject = new JSONObject();
            try {
                MyBeanUtil.copyProperties2Map(cart, jsonObject);
                Goods goods = goodsIdMap.get(cart.getGoodsId());
                if (goods.getIsNew()) {
                    jsonObject.put("tag", "新品");
                }
                if (goods.getIsHot()) {
                    jsonObject.put("tag", "热品");
                }
            } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
            array.add(jsonObject);
        }
        return R.success().add("data", array);
    }

    @Override
    public R changeNum(Long cartId, Integer number) {
        Cart cart = getById(cartId);
        Long productId = cart.getProductId();
        GoodsProduct goodsProduct = iGoodsProductService.getById(productId);
        Integer productNumber = goodsProduct.getNumber();
        if (number > productNumber) {
            throw new BusinessException(String.format("库存不足，该商品只剩%d件了", productNumber));
        }
        boolean update = lambdaUpdate().setSql("number = " + number).eq(Cart::getId, cartId).update();
        return R.result(update);
    }

    @Override
    public R addDefaultGoodsProduct(Cart cart) {
        Long goodsId = cart.getGoodsId();
        List<GoodsProduct> products = iGoodsProductService.list(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        List<GoodsProduct> goodsProducts = products.stream().filter(GoodsProduct::getDefaultSelected).collect(Collectors.toList());
        GoodsProduct defaultProduct;
        // 如果默认选中货品不为空则取默认选中货品，否则取第一个货品
        if (CollectionUtils.isNotEmpty(goodsProducts)) {
            defaultProduct = goodsProducts.get(0);
        } else {
            defaultProduct = products.get(0);
        }
        cart.setProductId(defaultProduct.getId());
        return this.add(cart);
    }
}
