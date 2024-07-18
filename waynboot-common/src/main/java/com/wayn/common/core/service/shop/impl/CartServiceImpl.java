package com.wayn.common.core.service.shop.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.entity.shop.Cart;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.mapper.shop.CartMapper;
import com.wayn.common.core.service.shop.ICartService;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.response.CartResponseVO;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisLock;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private ThreadPoolTaskExecutor commonThreadPoolTaskExecutor;

    @Override
    public Cart checkExistsGoods(Long userId, Long goodsId, Long productId) {
        return getOne(new QueryWrapper<Cart>()
                .eq("user_id", userId)
                .eq("goods_id", goodsId)
                .eq("product_id", productId));
    }

    @Override
    public void add(Cart cart, Long userId) {
        Long goodsId = cart.getGoodsId();
        Long productId = cart.getProductId();
        Integer number = cart.getNumber();
        if (!ObjectUtils.allNotNull(goodsId, productId, number) || number <= 0) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
        Goods goods = iGoodsService.getById(goodsId);
        if (!goods.getIsOnSale()) {
            throw new BusinessException(ReturnCodeEnum.GOODS_HAS_OFFSHELF_ERROR);
        }
        GoodsProduct product = iGoodsProductService.getById(productId);
        boolean lock = redisLock.lock(RedisKeyEnum.CART_LOCK.getKey(userId), 2);
        if (!lock) {
            throw new BusinessException("加锁失败");
        }
        try {
            Cart existsCart = checkExistsGoods(userId, goodsId, productId);
            if (Objects.isNull(existsCart)) {
                if (Objects.isNull(product) || number > product.getNumber()) {
                    throw new BusinessException(ReturnCodeEnum.GOODS_STOCK_NOT_ENOUGH_ERROR);
                }
                cart.setGoodsSn(goods.getGoodsSn());
                cart.setGoodsName(goods.getName());
                cart.setPicUrl(goods.getPicUrl());
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
                    throw new BusinessException(ReturnCodeEnum.GOODS_STOCK_NOT_ENOUGH_ERROR);
                }
                existsCart.setNumber(num);
                cart.setUpdateTime(LocalDateTime.now());
                updateById(existsCart);
            }
        } finally {
            redisLock.unLock(RedisKeyEnum.CART_LOCK.getKey(userId));
        }
    }

    @Override
    public Long goodsCount(Long userId) {
        // 用户未登录时，直接返回0
        if (userId == null) {
            return 0L;
        }
        return count(Wrappers.lambdaQuery(Cart.class).eq(Cart::getUserId, userId));
    }

    @Override
    public List<CartResponseVO> list(Page<Cart> page, Long userId) {
        IPage<Cart> goodsIPage = cartMapper.selectCartPageList(page, userId);
        List<Cart> cartList = goodsIPage.getRecords();
        List<Long> goodsIdList = cartList.stream().map(Cart::getGoodsId).toList();
        List<Long> productIdList = cartList.stream().map(Cart::getProductId).toList();

        List<CartResponseVO> responseVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(goodsIdList)) {
            return responseVOS;
        }
        Map<Long, GoodsProduct> productIdMap = iGoodsProductService.selectProductByIds(productIdList).stream()
                .collect(Collectors.toMap(GoodsProduct::getId, product -> product));

        for (Cart cart : cartList) {
            CartResponseVO responseVO = new CartResponseVO();
            GoodsProduct product = productIdMap.get(cart.getProductId());
            if (product != null) {
                Integer number = cart.getNumber();
                Integer maxNumber = product.getNumber();
                if (maxNumber < number) {
                    commonThreadPoolTaskExecutor.execute(() -> {
                        this.lambdaUpdate()
                                .set(Cart::getChecked, false)
                                .eq(Cart::getId, cart.getId())
                                .update();
                    });
                }
                BeanUtil.copyProperties(cart, responseVO);
                responseVO.setMaxNum(maxNumber);
                responseVOS.add(responseVO);
            }
        }
        return responseVOS;
    }

    @Override
    public Boolean changeNum(Long cartId, Integer number) {
        // todo 并发问题，后续可以通过 mq 实现
        commonThreadPoolTaskExecutor.execute(() -> {
            lambdaUpdate().setSql("number = " + number).eq(Cart::getId, cartId).update();
        });
        return true;
    }

    @Override
    public void addDefaultGoodsProduct(Cart cart, Long userId) {
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
        this.add(cart, userId);
    }
}
