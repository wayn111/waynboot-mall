package com.wayn.mobile.api.service;

import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.Cart;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 购物车商品表 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-08-03
 */
public interface ICartService extends IService<Cart> {

    /**
     * 检查用户购物车中是否有商品货品
     * @param userId
     * @param goodsId
     * @param productId
     * @return
     */
    Cart checkExistsGoods(Long userId, Integer goodsId, Integer productId);

    /**
     * 加入商品到购物车
     * <p>
     * 如果已经存在购物车货品，则增加数量；
     * 否则添加新的购物车货品项。
     *
     * @param cart 购物车商品信息， { goodsId: xxx, productId: xxx, number: xxx }
     * @return R
     */
    R addCart(Cart cart);

    /**
     * 计算购物车中商品数量
     * @return R
     */
    R goodsCount();

    /**
     * 查询用户购物车商品
     * @param userId 用户ID
     * @return R
     */
    R list(Long userId);

    R addNum(Long cartId, Integer number);

    R minusNum(Long cartId, Integer number);
}
