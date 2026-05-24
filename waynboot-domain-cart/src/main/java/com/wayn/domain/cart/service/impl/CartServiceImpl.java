package com.wayn.domain.cart.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.domain.api.cart.entity.Cart;
import com.wayn.domain.api.cart.mapper.CartMapper;
import com.wayn.domain.api.cart.service.ICartService;
import com.wayn.domain.cart.support.CartReadSupport;
import com.wayn.domain.cart.support.CartWriteSupport;
import com.wayn.domain.api.cart.response.CartResponseVO;
import com.wayn.domain.api.cart.response.CheckedGoodsResVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车服务外观层。
 * 读路径和写路径已经拆到独立支撑服务，这里仅保留对外兼容接口。
 */
@Service
@AllArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    private final CartWriteSupport cartWriteSupport;
    private final CartReadSupport cartReadSupport;

    /**
     * 委托查询购物车中是否存在相同商品。
     *
     * @param userId 用户 ID
     * @param goodsId 商品 ID
     * @param productId 货品 ID
     * @return 购物车项
     */
    @Override
    public Cart checkExistsGoods(Long userId, Long goodsId, Long productId) {
        return cartWriteSupport.checkExistsGoods(userId, goodsId, productId);
    }

    /**
     * 委托添加购物车。
     *
     * @param cart 购物车请求
     * @param userId 用户 ID
     */
    @Override
    public void add(Cart cart, Long userId) {
        cartWriteSupport.add(cart, userId);
    }

    /**
     * 委托统计购物车数量。
     *
     * @param userId 用户 ID
     * @return 商品数
     */
    @Override
    public Long goodsCount(Long userId) {
        return cartReadSupport.goodsCount(userId);
    }

    /**
     * 委托查询购物车列表。
     *
     * @param page 分页参数
     * @param userId 用户 ID
     * @return 购物车列表
     */
    @Override
    public List<CartResponseVO> list(Page<Cart> page, Long userId) {
        return cartReadSupport.list(page, userId);
    }

    /**
     * 委托修改购物车数量。
     *
     * @param cartId 购物车 ID
     * @param number 目标数量
     * @return 是否修改成功
     */
    @Override
    public Boolean changeNum(Long cartId, Integer number) {
        return cartWriteSupport.changeNum(cartId, number);
    }

    /**
     * 委托修改购物车勾选状态。
     *
     * @param cartId 购物车 ID
     * @param checked 勾选状态
     * @param userId 用户 ID
     * @return 是否修改成功
     */
    @Override
    public Boolean updateChecked(Long cartId, Boolean checked, Long userId) {
        return cartWriteSupport.updateChecked(cartId, checked, userId);
    }

    /**
     * 委托按默认货品加入购物车。
     *
     * @param cart 购物车请求
     * @param userId 用户 ID
     */
    @Override
    public void addDefaultGoodsProduct(Cart cart, Long userId) {
        cartWriteSupport.addDefaultGoodsProduct(cart, userId);
    }

    /**
     * 委托查询已勾选商品汇总。
     *
     * @param userId 用户 ID
     * @return 汇总结果
     */
    @Override
    public CheckedGoodsResVO getCheckedGoods(Long userId) {
        return cartReadSupport.getCheckedGoods(userId);
    }
}
