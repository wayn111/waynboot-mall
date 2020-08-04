package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.mobile.api.domain.Cart;
import com.wayn.mobile.api.mapper.CartMapper;
import com.wayn.mobile.api.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public boolean checkExistsGoods(Long userId, Integer goodsId, Integer productId) {
        Cart cart = cartMapper.selectOne(new QueryWrapper<Cart>()
                .eq("user_id", userId)
                .eq("goods_id", goodsId)
                .eq("product_id", productId));
        return false;
    }
}
