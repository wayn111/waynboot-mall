package com.wayn.mobile.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.Cart;

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
     *
     * @param userId
     * @param goodsId
     * @param productId
     * @return
     */
    Cart checkExistsGoods(Long userId, Long goodsId, Long productId);

    /**
     * 加入商品到购物车
     * <p>
     * 如果已经存在购物车货品，则增加数量；
     * 否则添加新的购物车货品项。
     *
     * @param cart 购物车商品信息， { goodsId: xxx, productId: xxx, number: xxx }
     * @return R
     */
    R add(Cart cart);

    /**
     * 计算购物车中商品数量
     *
     * @return R
     */
    R goodsCount();

    /**
     * 查询用户购物车商品
     *
     * @param userId 用户ID
     * @return R
     */
    R list(Long userId);

    /**
     * 改变购物车商品数量
     *
     * @param cartId 购物车商品ID
     * @param number 商品数量
     * @return r
     */
    R changeNum(Long cartId, Integer number);

    /**
     * 添加商品的默认选中货品至购物车
     * @param cart 购物车对象
     * @return r
     */
    R addDefaultGoodsProduct(Cart cart);
}
