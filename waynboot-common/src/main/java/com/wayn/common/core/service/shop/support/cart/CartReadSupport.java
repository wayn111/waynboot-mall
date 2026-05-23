package com.wayn.common.core.service.shop.support.cart;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.entity.shop.Cart;
import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.entity.shop.ShopMemberCoupon;
import com.wayn.common.core.mapper.shop.CartMapper;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.ShopMemberCouponService;
import com.wayn.common.model.response.CartCheckedItemResVO;
import com.wayn.common.model.response.CartResponseVO;
import com.wayn.common.model.response.CheckedGoodsResVO;
import com.wayn.common.model.response.MemberCouponResVO;
import com.wayn.common.config.WaynConfig;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车读路径支撑服务。
 * 负责购物车列表展示和数量统计，并在读取时同步修正失效勾选状态。
 */
@Service
@AllArgsConstructor
public class CartReadSupport {

    private final CartMapper cartMapper;
    private final IGoodsProductService goodsProductService;
    private final ShopMemberCouponService shopMemberCouponService;

    /**
     * 统计用户购物车商品数。
     *
     * @param userId 用户 ID
     * @return 商品数
     */
    public Long goodsCount(Long userId) {
        if (userId == null) {
            return 0L;
        }
        return cartMapper.selectCount(Wrappers.lambdaQuery(Cart.class)
                .eq(Cart::getUserId, userId));
    }

    /**
     * 查询购物车列表并补齐最大库存。
     * 读取过程中如果发现货品失效或库存不足，会同步把对应购物车项取消勾选。
     *
     * @param page 分页参数
     * @param userId 用户 ID
     * @return 购物车列表
     */
    public List<CartResponseVO> list(Page<Cart> page, Long userId) {
        IPage<Cart> cartPage = cartMapper.selectCartPageList(page, userId);
        List<Cart> cartList = cartPage.getRecords();
        if (CollectionUtils.isEmpty(cartList)) {
            return new ArrayList<>();
        }

        List<Long> productIdList = cartList.stream().map(Cart::getProductId).toList();
        Map<Long, GoodsProduct> productMap = goodsProductService.selectProductByIds(productIdList).stream()
                .collect(Collectors.toMap(GoodsProduct::getId, product -> product));

        List<CartResponseVO> responseVOList = new ArrayList<>(cartList.size());
        List<Long> uncheckedCartIds = new ArrayList<>();
        for (Cart cart : cartList) {
            GoodsProduct product = productMap.get(cart.getProductId());
            if (product == null) {
                uncheckedCartIds.add(cart.getId());
                continue;
            }
            CartResponseVO responseVO = new CartResponseVO();
            BeanUtil.copyProperties(cart, responseVO);
            responseVO.setMaxNum(product.getNumber());
            if (product.getNumber() < cart.getNumber()) {
                // 列表展示时发现库存不足，直接把该购物车项置为未勾选，避免继续参与结算。
                responseVO.setChecked(false);
                uncheckedCartIds.add(cart.getId());
            }
            responseVOList.add(responseVO);
        }

        if (CollectionUtils.isNotEmpty(uncheckedCartIds)) {
            cartMapper.update(null, Wrappers.lambdaUpdate(Cart.class)
                    .set(Cart::getChecked, false)
                    .set(Cart::getUpdateTime, LocalDateTime.now())
                    .in(Cart::getId, uncheckedCartIds));
        }
        return responseVOList;
    }

    /**
     * 查询已勾选商品结算信息。
     * 会过滤失效或库存不足的购物车项，并同步把这些项改回未勾选状态，避免继续参与结算。
     *
     * @param userId 用户 ID
     * @return 结算汇总
     */
    public CheckedGoodsResVO getCheckedGoods(Long userId) {
        List<Cart> checkedCartList = cartMapper.selectList(Wrappers.lambdaQuery(Cart.class)
                .eq(Cart::getUserId, userId)
                .eq(Cart::getChecked, true));
        if (CollectionUtils.isEmpty(checkedCartList)) {
            return buildCheckedGoodsResVO(List.of(), List.of(), BigDecimal.ZERO, BigDecimal.ZERO);
        }

        List<Long> productIdList = checkedCartList.stream().map(Cart::getProductId).toList();
        Map<Long, GoodsProduct> productMap = goodsProductService.selectProductByIds(productIdList).stream()
                .collect(Collectors.toMap(GoodsProduct::getId, product -> product));

        CheckedGoodsAggregation aggregation = aggregateCheckedGoods(checkedCartList, productMap);

        syncUncheckedCartItems(aggregation.uncheckedCartIds());
        if (aggregation.checkedItems().isEmpty()) {
            return buildCheckedGoodsResVO(List.of(), List.of(), BigDecimal.ZERO, BigDecimal.ZERO);
        }
        BigDecimal freightPrice = calculateFreightPrice(aggregation.goodsAmount());
        BigDecimal orderTotalAmount = aggregation.goodsAmount().add(freightPrice).max(BigDecimal.ZERO);
        List<MemberCouponResVO> couponList = selectAvailableCoupons(userId, orderTotalAmount);
        return buildCheckedGoodsResVO(aggregation.checkedItems(), couponList, freightPrice, orderTotalAmount);
    }

    /**
     * 聚合有效勾选商品并收集需要取消勾选的购物车项。
     * 这里不直接写库，避免“过滤有效商品”和“同步购物车状态”两个副作用混在同一段循环中。
     *
     * @param checkedCartList 已勾选购物车项
     * @param productMap 货品映射
     * @return 有效商品、失效购物车 ID 和商品金额汇总
     */
    private CheckedGoodsAggregation aggregateCheckedGoods(List<Cart> checkedCartList,
                                                          Map<Long, GoodsProduct> productMap) {
        List<Long> uncheckedCartIds = new ArrayList<>();
        List<CartCheckedItemResVO> checkedItems = new ArrayList<>();
        BigDecimal goodsAmount = BigDecimal.ZERO;
        for (Cart cart : checkedCartList) {
            GoodsProduct product = productMap.get(cart.getProductId());
            if (!isValidCheckedCart(cart, product)) {
                uncheckedCartIds.add(cart.getId());
                continue;
            }
            checkedItems.add(buildCheckedItem(cart, product));
            goodsAmount = goodsAmount.add(cart.getPrice().multiply(BigDecimal.valueOf(cart.getNumber())));
        }
        return new CheckedGoodsAggregation(checkedItems, uncheckedCartIds, goodsAmount);
    }

    /**
     * 判断勾选购物车项是否仍可参与结算。
     *
     * @param cart 购物车项
     * @param product 货品信息
     * @return true 表示货品存在且库存覆盖购物车数量
     */
    private boolean isValidCheckedCart(Cart cart, GoodsProduct product) {
        return product != null && product.getNumber() >= cart.getNumber();
    }

    /**
     * 构建结算商品项。
     *
     * @param cart 购物车项
     * @param product 货品信息
     * @return 结算商品 VO
     */
    private CartCheckedItemResVO buildCheckedItem(Cart cart, GoodsProduct product) {
        CartCheckedItemResVO itemResVO = new CartCheckedItemResVO();
        BeanUtil.copyProperties(cart, itemResVO);
        itemResVO.setMaxNum(product.getNumber());
        return itemResVO;
    }

    /**
     * 计算运费。
     *
     * @param goodsAmount 商品金额
     * @return 运费金额
     */
    private BigDecimal calculateFreightPrice(BigDecimal goodsAmount) {
        return goodsAmount.compareTo(WaynConfig.getFreightLimit()) < 0
                ? WaynConfig.getFreightPrice()
                : BigDecimal.ZERO;
    }

    /**
     * 查询当前订单金额可用的优惠券。
     *
     * @param userId 用户 ID
     * @param orderTotalAmount 订单总金额
     * @return 可用优惠券列表
     */
    private List<MemberCouponResVO> selectAvailableCoupons(Long userId, BigDecimal orderTotalAmount) {
        Date nowTime = new Date();
        return shopMemberCouponService.lambdaQuery()
                .eq(ShopMemberCoupon::getUserId, userId)
                .list()
                .stream()
                .filter(item -> isAvailableCoupon(item, orderTotalAmount, nowTime))
                .sorted(Comparator.comparingInt(ShopMemberCoupon::getDiscount).reversed())
                .map(item -> BeanUtil.copyProperties(item, MemberCouponResVO.class))
                .toList();
    }

    /**
     * 判断优惠券是否可用于当前订单金额。
     *
     * @param coupon 用户优惠券
     * @param orderTotalAmount 订单总金额
     * @param nowTime 当前时间
     * @return true 表示优惠券未使用、未过期且满足门槛
     */
    private boolean isAvailableCoupon(ShopMemberCoupon coupon, BigDecimal orderTotalAmount, Date nowTime) {
        return coupon.getUseStatus() == 0
                && coupon.getExpireTime() != null
                && DateUtil.compare(coupon.getExpireTime(), nowTime) > 0
                && (coupon.getMin() == null || orderTotalAmount.compareTo(BigDecimal.valueOf(coupon.getMin())) >= 0);
    }

    /**
     * 同步把无效购物车项置为未勾选。
     *
     * @param uncheckedCartIds 需要取消勾选的购物车 ID
     */
    private void syncUncheckedCartItems(List<Long> uncheckedCartIds) {
        if (CollectionUtils.isNotEmpty(uncheckedCartIds)) {
            cartMapper.update(null, Wrappers.lambdaUpdate(Cart.class)
                    .set(Cart::getChecked, false)
                    .set(Cart::getUpdateTime, LocalDateTime.now())
                    .in(Cart::getId, uncheckedCartIds));
        }
    }

    /**
     * 构建已勾选商品汇总返回。
     *
     * @param checkedItems 已勾选购物车项
     * @param couponList 可用优惠券
     * @param freightPrice 运费
     * @param orderTotalAmount 订单总价
     * @return 汇总结果
     */
    private CheckedGoodsResVO buildCheckedGoodsResVO(List<CartCheckedItemResVO> checkedItems,
                                                     List<MemberCouponResVO> couponList,
                                                     BigDecimal freightPrice,
                                                     BigDecimal orderTotalAmount) {
        CheckedGoodsResVO resVO = new CheckedGoodsResVO();
        resVO.setData(checkedItems);
        resVO.setCouponList(couponList);
        resVO.setFreightPrice(freightPrice);
        resVO.setGoodsAmount(orderTotalAmount.subtract(freightPrice));
        resVO.setOrderTotalAmount(orderTotalAmount);
        return resVO;
    }

    /**
     * 已勾选商品聚合结果。
     *
     * @param checkedItems 可参与结算的商品项
     * @param uncheckedCartIds 需要取消勾选的购物车 ID
     * @param goodsAmount 商品总金额
     */
    private record CheckedGoodsAggregation(List<CartCheckedItemResVO> checkedItems,
                                           List<Long> uncheckedCartIds,
                                           BigDecimal goodsAmount) {
    }
}
