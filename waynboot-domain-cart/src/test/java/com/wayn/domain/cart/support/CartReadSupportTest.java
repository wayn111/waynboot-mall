package com.wayn.domain.cart.support;

import com.wayn.domain.api.common.MybatisPlusTableInfoTestHelper;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.wayn.domain.api.common.WaynConfig;
import com.wayn.domain.api.cart.entity.Cart;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.promotion.entity.ShopMemberCoupon;
import com.wayn.domain.api.cart.mapper.CartMapper;
import com.wayn.domain.api.goods.service.IGoodsProductService;
import com.wayn.domain.api.promotion.service.ShopMemberCouponService;
import com.wayn.domain.api.cart.response.CheckedGoodsResVO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartReadSupportTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(Cart.class, ShopMemberCoupon.class);
    }

    @Mock
    private CartMapper cartMapper;
    @Mock
    private IGoodsProductService goodsProductService;
    @Mock
    private ShopMemberCouponService shopMemberCouponService;
    @Mock(answer = Answers.RETURNS_SELF)
    @SuppressWarnings("rawtypes")
    private LambdaQueryChainWrapper couponQueryWrapper;

    @BeforeEach
    void setUp() {
        WaynConfig config = new WaynConfig();
        config.setFreightLimit(new BigDecimal("50.00"));
        config.setFreightPrice(new BigDecimal("8.00"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCheckedGoodsFiltersInvalidItemsAndBuildsSettlementSummary() {
        CartReadSupport support = new CartReadSupport(cartMapper, goodsProductService, shopMemberCouponService);
        Cart validCart = buildCart(1L, 9, 101L, 1001L, new BigDecimal("10.00"), 2);
        Cart invalidCart = buildCart(2L, 9, 102L, 1002L, new BigDecimal("8.00"), 5);
        GoodsProduct validProduct = buildProduct(1001L, 101L, 9);
        GoodsProduct invalidProduct = buildProduct(1002L, 102L, 4);

        ShopMemberCoupon availableCoupon = buildCoupon(1, 20, 10, 0, 2);
        ShopMemberCoupon unavailableCoupon = buildCoupon(2, 10, 40, 0, 2);

        when(cartMapper.selectList(any())).thenReturn(List.of(validCart, invalidCart));
        when(goodsProductService.selectProductByIds(List.of(1001L, 1002L))).thenReturn(List.of(validProduct, invalidProduct));
        when(cartMapper.update(any(), any())).thenReturn(1);
        when(shopMemberCouponService.lambdaQuery()).thenReturn(couponQueryWrapper);
        when(couponQueryWrapper.eq(any(), any())).thenReturn(couponQueryWrapper);
        when(couponQueryWrapper.list()).thenReturn(List.of(availableCoupon, unavailableCoupon));

        CheckedGoodsResVO resVO = support.getCheckedGoods(9L);

        assertEquals(1, resVO.getData().size());
        assertEquals(new BigDecimal("20.00"), resVO.getGoodsAmount());
        assertEquals(new BigDecimal("8.00"), resVO.getFreightPrice());
        assertEquals(new BigDecimal("28.00"), resVO.getOrderTotalAmount());
        assertEquals(1, resVO.getCouponList().size());
        assertTrue(resVO.getCouponList().stream().allMatch(item -> item.getId() != null));
        verify(cartMapper).update(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCheckedGoodsReturnsZeroAmountWhenAllCheckedItemsBecomeInvalid() {
        CartReadSupport support = new CartReadSupport(cartMapper, goodsProductService, shopMemberCouponService);
        Cart invalidCart = buildCart(2L, 9, 102L, 1002L, new BigDecimal("8.00"), 5);
        GoodsProduct invalidProduct = buildProduct(1002L, 102L, 4);

        when(cartMapper.selectList(any())).thenReturn(List.of(invalidCart));
        when(goodsProductService.selectProductByIds(List.of(1002L))).thenReturn(List.of(invalidProduct));
        when(cartMapper.update(any(), any())).thenReturn(1);

        CheckedGoodsResVO resVO = support.getCheckedGoods(9L);

        assertTrue(resVO.getData().isEmpty());
        assertTrue(resVO.getCouponList().isEmpty());
        assertEquals(BigDecimal.ZERO, resVO.getGoodsAmount());
        assertEquals(BigDecimal.ZERO, resVO.getFreightPrice());
        assertEquals(BigDecimal.ZERO, resVO.getOrderTotalAmount());
        verify(cartMapper).update(any(), any());
    }

    private Cart buildCart(Long id, Integer userId, Long goodsId, Long productId, BigDecimal price, int number) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setUserId(userId);
        cart.setGoodsId(goodsId);
        cart.setProductId(productId);
        cart.setPrice(price);
        cart.setNumber(number);
        cart.setChecked(true);
        cart.setCreateTime(LocalDateTime.now());
        return cart;
    }

    private GoodsProduct buildProduct(Long id, Long goodsId, int stock) {
        GoodsProduct product = new GoodsProduct();
        product.setId(id);
        product.setGoodsId(goodsId);
        product.setNumber(stock);
        return product;
    }

    private ShopMemberCoupon buildCoupon(Integer id, Integer discount, Integer min, Integer useStatus, int expireAfterDays) {
        ShopMemberCoupon coupon = new ShopMemberCoupon();
        coupon.setId(id.longValue());
        coupon.setDiscount(discount);
        coupon.setMin(min);
        coupon.setUseStatus(useStatus);
        coupon.setExpireTime(new Date(System.currentTimeMillis() + expireAfterDays * 24L * 60 * 60 * 1000));
        return coupon;
    }
}
