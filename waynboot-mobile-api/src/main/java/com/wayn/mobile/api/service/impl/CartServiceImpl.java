package com.wayn.mobile.api.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.shop.GoodsProduct;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.util.R;
import com.wayn.common.util.bean.MyBeanUtil;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisLock;
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
 * 购物车商品表 服务实现类
 *
 * @author wayn
 * @since 2020-08-03
 */
@Service
@AllArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    private RedisLock redisLock;

    private IGoodsService iGoodsService;

    private IGoodsProductService iGoodsProductService;

    private CartMapper cartMapper;

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
        boolean lock = redisLock.lock(RedisKeyEnum.CART_LOCK.getKey(userId), 2);
        if (!lock) {
            throw new BusinessException("加锁失败");
        }
        try {
            Cart existsCart = checkExistsGoods(userId, goodsId, productId);
            if (Objects.isNull(existsCart)) {
                if (Objects.isNull(product) || number > product.getNumber()) {
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
        } finally {
            redisLock.unLock(RedisKeyEnum.CART_LOCK.getKey(userId));
        }
        return R.success();
    }

    @Override
    public R goodsCount() {
        Long userId = MobileSecurityUtils.getUserId();
        // 用户未登录时，直接返回0
        if (userId == null) {
            return R.success().add("count", 0);
        }
        long count = count(Wrappers.lambdaQuery(Cart.class).eq(Cart::getUserId, userId));
        return R.success().add("count", count);
    }

    @Override
    public R list(Page<Cart> page, Long userId) {
        IPage<Cart> goodsIPage = cartMapper.selectCartPageList(page, userId);
        List<Cart> cartList = goodsIPage.getRecords();
        List<Long> goodsIdList = cartList.stream().map(Cart::getGoodsId).collect(Collectors.toList());

        JSONArray array = new JSONArray();
        if (CollectionUtils.isEmpty(goodsIdList)) {
            return R.success().add("data", array);
        }
        Map<Long, Goods> goodsIdMap = iGoodsService.selectGoodsByIds(goodsIdList).stream().collect(Collectors.toMap(Goods::getId, goods -> goods));

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
