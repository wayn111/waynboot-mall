package com.wayn.mobile.api.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.config.WaynConfig;
import com.wayn.common.core.entity.shop.Cart;
import com.wayn.common.core.entity.shop.ShopMemberCoupon;
import com.wayn.common.core.service.shop.ICartService;
import com.wayn.common.core.service.shop.ShopMemberCouponService;
import com.wayn.common.response.CartResponseVO;
import com.wayn.common.response.CheckedGoodsResVO;
import com.wayn.common.response.MemberCouponResVO;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 购物车接口
 *
 * @author wayn
 * @since 2020-08-03
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("cart")
public class CartController extends BaseController {

    private ICartService iCartService;
    private ShopMemberCouponService shopMemberCouponService;

    /**
     * 购物车列表
     *
     * @return
     */
    @GetMapping("list")
    public R<List<CartResponseVO>> list() {
        Long userId = MobileSecurityUtils.getUserId();
        Page<Cart> page = getPage();
        return R.success(iCartService.list(page, userId));
    }

    /**
     * 添加购物车
     *
     * @return R
     */
    @PostMapping
    public R<Boolean> add(@RequestBody Cart cart) {
        iCartService.add(cart, MobileSecurityUtils.getUserId());
        return R.success();
    }

    /**
     * 往购物车中添加默认商品
     *
     * @return R
     */
    @PostMapping("addDefaultGoodsProduct")
    public R<Boolean> addDefaultGoodsProduct(@RequestBody Cart cart) {
        iCartService.addDefaultGoodsProduct(cart, MobileSecurityUtils.getUserId());
        return R.success();
    }

    /**
     * 更新购物车
     *
     * @param cart 更新参数
     * @return R
     */
    @PutMapping
    public R<Boolean> update(@RequestBody Cart cart) {
        return R.result(iCartService.updateById(cart));
    }

    /**
     * 修改购物车商品数量
     *
     * @param cartId 购物车id
     * @param number 更新数量
     * @return R
     */
    @PostMapping("changeNum/{cartId}/{number}")
    public R<Boolean> changeNum(@PathVariable Long cartId, @PathVariable Integer number) {
        return R.result(iCartService.changeNum(cartId, number));
    }

    /**
     * 删除购物车商品
     *
     * @param cartId 购物车id
     * @return R
     */
    @DeleteMapping("{cartId}")
    public R<Boolean> delete(@PathVariable Long cartId) {
        return R.result(iCartService.removeById(cartId));
    }

    /**
     * 统计购物车中现有商品数量
     *
     * @return R
     */
    @GetMapping("goodsCount")
    public R<Long> goodsCount() {
        return R.success(iCartService.goodsCount(MobileSecurityUtils.getUserId()));
    }

    /**
     * 统计购物车中已勾选商品数量
     *
     * @return R
     */
    @PostMapping("getCheckedGoods")
    public R<CheckedGoodsResVO> getCheckedGoods() {
        Long userId = MobileSecurityUtils.getUserId();
        Date nowTime = new Date();
        List<Cart> cartList = iCartService.list(new QueryWrapper<Cart>()
                .eq("user_id", userId).eq("checked", true));
        BigDecimal goodsAmount = BigDecimal.ZERO;
        BigDecimal orderActualAmount = BigDecimal.ZERO;
        // 计算总价
        for (Cart cart : cartList) {
            goodsAmount = goodsAmount.add(cart.getPrice().multiply(new BigDecimal(cart.getNumber())));
        }

        // 根据订单商品总价计算运费，满足条件（例如88元）则免运费，否则需要支付运费（例如8元）；
        BigDecimal freightPrice = BigDecimal.ZERO;
        if (goodsAmount.compareTo(WaynConfig.getFreightLimit()) < 0) {
            freightPrice = WaynConfig.getFreightPrice();
        }
        goodsAmount = goodsAmount.add(freightPrice);
        // 查询可用优惠券列表
        List<ShopMemberCoupon> memberCoupons = shopMemberCouponService.lambdaQuery()
                .eq(ShopMemberCoupon::getUserId, userId)
                .list();
        BigDecimal finalGoodsAmount = goodsAmount;
        orderActualAmount = goodsAmount.max(BigDecimal.ZERO);
        memberCoupons = memberCoupons.stream().filter(item -> item.getUseStatus() == 0
                        && DateUtil.compare(item.getExpireTime(), nowTime) > 0
                        && finalGoodsAmount.compareTo(new BigDecimal(item.getMin())) >= 0)
                .toList();
        if (!memberCoupons.isEmpty()) {
            memberCoupons = memberCoupons.stream().sorted(Comparator.comparingInt(ShopMemberCoupon::getDiscount).reversed()).toList();
        }
        CheckedGoodsResVO resVO = new CheckedGoodsResVO();
        resVO.setData(cartList);
        resVO.setCouponList(BeanUtil.copyToList(memberCoupons, MemberCouponResVO.class));
        resVO.setFreightPrice(freightPrice);
        resVO.setGoodsAmount(goodsAmount);
        resVO.setOrderTotalAmount(orderActualAmount);
        return R.success(resVO);
    }
}
