package com.wayn.mobile.api.service;

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
    boolean checkExistsGoods(Long userId, Integer goodsId, Integer productId);
}
