package com.wayn.common.core.service.shop.support;

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
        Date nowTime = new Date();
        List<Cart> checkedCartList = cartMapper.selectList(Wrappers.lambdaQuery(Cart.class)
                .eq(Cart::getUserId, userId)
                .eq(Cart::getChecked, true));
        if (CollectionUtils.isEmpty(checkedCartList)) {
            return buildCheckedGoodsResVO(List.of(), List.of(), BigDecimal.ZERO, BigDecimal.ZERO);
        }

        List<Long> productIdList = checkedCartList.stream().map(Cart::getProductId).toList();
        Map<Long, GoodsProduct> productMap = goodsProductService.selectProductByIds(productIdList).stream()
                .collect(Collectors.toMap(GoodsProduct::getId, product -> product));

        List<Long> uncheckedCartIds = new ArrayList<>();
        List<CartCheckedItemResVO> checkedItems = new ArrayList<>();
        BigDecimal goodsAmount = BigDecimal.ZERO;
        for (Cart cart : checkedCartList) {
            GoodsProduct product = productMap.get(cart.getProductId());
            if (product == null || product.getNumber() < cart.getNumber()) {
                uncheckedCartIds.add(cart.getId());
                continue;
            }
            CartCheckedItemResVO itemResVO = new CartCheckedItemResVO();
            BeanUtil.copyProperties(cart, itemResVO);
            itemResVO.setMaxNum(product.getNumber());
            checkedItems.add(itemResVO);
            goodsAmount = goodsAmount.add(cart.getPrice().multiply(BigDecimal.valueOf(cart.getNumber())));
        }

        syncUncheckedCartItems(uncheckedCartIds);
        if (checkedItems.isEmpty()) {
            return buildCheckedGoodsResVO(List.of(), List.of(), BigDecimal.ZERO, BigDecimal.ZERO);
        }
        BigDecimal freightPrice = goodsAmount.compareTo(WaynConfig.getFreightLimit()) < 0
                ? WaynConfig.getFreightPrice()
                : BigDecimal.ZERO;
        BigDecimal orderTotalAmount = goodsAmount.add(freightPrice).max(BigDecimal.ZERO);

        List<MemberCouponResVO> couponList = shopMemberCouponService.lambdaQuery()
                .eq(ShopMemberCoupon::getUserId, userId)
                .list()
                .stream()
                .filter(item -> item.getUseStatus() == 0
                        && item.getExpireTime() != null
                        && DateUtil.compare(item.getExpireTime(), nowTime) > 0
                        && (item.getMin() == null || orderTotalAmount.compareTo(BigDecimal.valueOf(item.getMin())) >= 0))
                .sorted(Comparator.comparingInt(ShopMemberCoupon::getDiscount).reversed())
                .map(item -> BeanUtil.copyProperties(item, MemberCouponResVO.class))
                .toList();
        return buildCheckedGoodsResVO(checkedItems, couponList, freightPrice, orderTotalAmount);
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
}
