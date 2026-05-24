package com.wayn.domain.api.cart.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.domain.api.cart.entity.Cart;
import com.wayn.domain.api.cart.response.CartResponseVO;
import com.wayn.domain.api.cart.response.CheckedGoodsResVO;

import java.util.List;

/**
 * 购物车商品表 服务类
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
    void add(Cart cart, Long userId);

    /**
     * 计算购物车中商品数量
     *
     * @return R
     */
    Long goodsCount(Long userId);

    /**
     * 查询用户购物车商品
     *
     * @param userId 用户ID
     * @return R
     */
    List<CartResponseVO> list(Page<Cart> page, Long userId);

    /**
     * 改变购物车商品数量
     *
     * @param cartId 购物车商品ID
     * @param number 商品数量
     * @return r
     */
    Boolean changeNum(Long cartId, Integer number);

    /**
     * 修改购物车勾选状态。
     *
     * @param cartId 购物车 ID
     * @param checked 勾选状态
     * @param userId 用户 ID
     * @return 是否修改成功
     */
    Boolean updateChecked(Long cartId, Boolean checked, Long userId);

    /**
     * 添加商品的默认选中货品至购物车
     *
     * @param cart 购物车对象
     * @return r
     */
    void addDefaultGoodsProduct(Cart cart, Long userId);

    /**
     * 查询已勾选商品汇总。
     *
     * @param userId 用户 ID
     * @return 已勾选商品汇总
     */
    CheckedGoodsResVO getCheckedGoods(Long userId);
}
