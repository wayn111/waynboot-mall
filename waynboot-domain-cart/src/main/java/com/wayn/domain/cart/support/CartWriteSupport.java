package com.wayn.domain.cart.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.domain.api.cart.entity.Cart;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.cart.mapper.CartMapper;
import com.wayn.domain.api.common.TradeLockSupport;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 购物车写路径支撑服务。
 * 所有会修改购物车状态的操作都从这里进入，统一加用户维度锁并做库存校验。
 */
@Service
@AllArgsConstructor
public class CartWriteSupport {

    private final TradeLockSupport tradeLockSupport;
    private final CartMapper cartMapper;
    private final CartValidationSupport cartValidationSupport;

    /**
     * 查询购物车中是否已存在相同商品和货品。
     *
     * @param userId 用户 ID
     * @param goodsId 商品 ID
     * @param productId 货品 ID
     * @return 购物车项
     */
    public Cart checkExistsGoods(Long userId, Long goodsId, Long productId) {
        return cartMapper.selectOne(Wrappers.lambdaQuery(Cart.class)
                .eq(Cart::getUserId, userId)
                .eq(Cart::getGoodsId, goodsId)
                .eq(Cart::getProductId, productId));
    }

    /**
     * 添加商品到购物车。
     * 同一用户的购物车写操作通过用户维度锁串行化，避免并发累加数量时出现覆盖。
     *
     * @param cart 购物车请求
     * @param userId 用户 ID
     */
    public void add(Cart cart, Long userId) {
        Long goodsId = cart.getGoodsId();
        Long productId = cart.getProductId();
        Integer number = cart.getNumber();
        cartValidationSupport.validateRequest(goodsId, productId, number);
        Goods goods = cartValidationSupport.requireOnSaleGoods(goodsId);
        GoodsProduct product = cartValidationSupport.requireProduct(productId, goodsId);
        String lockKey = RedisKeyEnum.CART_LOCK.getKey(userId);
        tradeLockSupport.runWithLock(lockKey, 2,
                () -> new BusinessException(ReturnCodeEnum.ERROR, "购物车操作频繁，请稍后重试"),
                () -> {
            Cart existsCart = checkExistsGoods(userId, goodsId, productId);
            if (Objects.isNull(existsCart)) {
                cartValidationSupport.ensureEnoughStock(product, number);
                fillCartSnapshot(cart, userId, goods, product);
                cartMapper.insert(cart);
                return;
            }

            int targetNumber = existsCart.getNumber() + number;
            cartValidationSupport.ensureEnoughStock(product, targetNumber);
            existsCart.setNumber(targetNumber);
            existsCart.setUpdateTime(LocalDateTime.now());
            cartMapper.updateById(existsCart);
        });
    }

    /**
     * 修改购物车数量。
     *
     * @param cartId 购物车 ID
     * @param number 目标数量
     * @return 是否修改成功
     */
    public Boolean changeNum(Long cartId, Integer number) {
        if (cartId == null || number == null || number <= 0) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }

        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
        GoodsProduct product = cartValidationSupport.requireProduct(cart.getProductId(), cart.getGoodsId());
        cartValidationSupport.ensureEnoughStock(product, number);

        String lockKey = RedisKeyEnum.CART_LOCK.getKey(cart.getUserId());
        return tradeLockSupport.executeWithLock(lockKey, 2,
                () -> new BusinessException(ReturnCodeEnum.ERROR, "购物车操作频繁，请稍后重试"),
                () -> {
            // 改数量改为同步更新，接口返回即代表数据库已落库，避免异步写导致前端读到旧值。
            Cart updateCart = new Cart();
            updateCart.setId(cartId);
            updateCart.setNumber(number);
            updateCart.setUpdateTime(LocalDateTime.now());
            return cartMapper.updateById(updateCart) > 0;
        });
    }

    /**
     * 修改购物车勾选状态。
     *
     * @param cartId 购物车 ID
     * @param checked 勾选状态
     * @param userId 用户 ID
     * @return 是否修改成功
     */
    public Boolean updateChecked(Long cartId, Boolean checked, Long userId) {
        if (cartId == null || checked == null || userId == null) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
        String lockKey = RedisKeyEnum.CART_LOCK.getKey(userId);
        return tradeLockSupport.executeWithLock(lockKey, 2,
                () -> new BusinessException(ReturnCodeEnum.ERROR, "购物车操作频繁，请稍后重试"),
                () -> cartMapper.update(null, Wrappers.lambdaUpdate(Cart.class)
                        .set(Cart::getChecked, checked)
                        .set(Cart::getUpdateTime, LocalDateTime.now())
                        .eq(Cart::getId, cartId)
                        .eq(Cart::getUserId, Math.toIntExact(userId))) > 0);
    }

    /**
     * 以商品默认货品加入购物车。
     *
     * @param cart 购物车请求
     * @param userId 用户 ID
     */
    public void addDefaultGoodsProduct(Cart cart, Long userId) {
        GoodsProduct defaultProduct = cartValidationSupport.resolveDefaultProduct(cart.getGoodsId());
        cart.setProductId(defaultProduct.getId());
        add(cart, userId);
    }

    /**
     * 填充购物车快照字段。
     *
     * @param cart 购物车对象
     * @param userId 用户 ID
     * @param goods 商品信息
     * @param product 货品信息
     */
    private void fillCartSnapshot(Cart cart, Long userId, Goods goods, GoodsProduct product) {
        cart.setGoodsSn(goods.getGoodsSn());
        cart.setGoodsName(goods.getName());
        cart.setPicUrl(goods.getPicUrl());
        cart.setPrice(product.getPrice());
        cart.setSpecifications(product.getSpecifications());
        cart.setUserId(Math.toIntExact(userId));
        cart.setChecked(true);
        cart.setRemark(goods.getBrief());
        cart.setCreateTime(LocalDateTime.now());
    }
}
