package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.shop.GoodsProduct;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.Cart;
import com.wayn.mobile.api.mapper.CartMapper;
import com.wayn.mobile.api.service.ICartService;
import com.wayn.mobile.framework.security.util.SecurityUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private IGoodsService iGoodsService;

    @Autowired
    private IGoodsProductService iGoodsProductService;

    @Override
    public Cart checkExistsGoods(Long userId, Long goodsId, Long productId) {
        return cartMapper.selectOne(new QueryWrapper<Cart>()
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
            return R.error("参数错误");
        }
        Goods goods = iGoodsService.getById(goodsId);
        if (Objects.isNull(iGoodsProductService) || !goods.getIsOnSale()) {
            return R.error("商品已经下架");
        }
        Long userId = SecurityUtils.getLoginUser().getMember().getId();
        GoodsProduct product = iGoodsProductService.getById(productId);
        Cart existsCart = checkExistsGoods(userId, goodsId, productId);
        if (Objects.isNull(existsCart)) {
            if (Objects.isNull(product) || product.getNumber() < number) {
                return R.error("库存不足");
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
                return R.error("库存不足");
            }
            existsCart.setNumber(num);
            cart.setUpdateTime(LocalDateTime.now());
            if (!updateById(existsCart)) {
                return R.error();
            }
        }
        return goodsCount();
    }

    @Override
    public R goodsCount() {
        Long userId = SecurityUtils.getLoginUser().getMember().getId();
        List<Cart> cartList = list(new QueryWrapper<Cart>()
                .eq("user_id", userId));
        return R.success().add("count", cartList.size());
    }

    @Override
    public R list(Long userId) {
        List<Cart> cartList = list(new QueryWrapper<Cart>()
                .eq("user_id", userId));
        return R.success().add("data", cartList);
    }

    @Override
    public R changeNum(Long cartId, Integer number) {
        return R.result(update().setSql("number = " + number).eq("id", cartId).update(), "修改失败");
    }

    @Override
    public R addNum(Long cartId, Integer number) {
        return R.result(update().setSql("number = number + 1").eq("id", cartId).update(), "添加失败");
    }

    @Override
    public R minusNum(Long cartId, Integer number) {
        return R.result(update().setSql("number = number - 1").eq("id", cartId).last("and number > 1").update(), "最少购买一件");
    }

    @Override
    public R addDefaultGoodsProduct(Cart cart) {
        Long goodsId = cart.getGoodsId();
        List<GoodsProduct> products = iGoodsProductService.list(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        List<GoodsProduct> goodsProducts = products.stream().filter(goodsProduct -> goodsProduct.getDefaultSelected()).collect(Collectors.toList());
        GoodsProduct defaultProduct;
        // 如果默认选中货品不为空则取默认选中货品，否则取第一个货品
        if (CollectionUtils.isNotEmpty(goodsProducts)) {
            defaultProduct = goodsProducts.get(0);
        } else {
            defaultProduct = products.get(0);
        }
        cart.setProductId(defaultProduct.getId());
        return add(cart);
    }
}
