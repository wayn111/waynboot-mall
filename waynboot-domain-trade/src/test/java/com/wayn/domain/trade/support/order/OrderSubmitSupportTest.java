package com.wayn.domain.trade.support.order;

import com.wayn.domain.api.common.MybatisPlusTableInfoTestHelper;

import com.wayn.domain.api.common.WaynConfig;
import com.wayn.domain.api.trade.entity.Address;
import com.wayn.domain.api.cart.entity.Cart;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.service.IAddressService;
import com.wayn.domain.api.cart.service.ICartService;
import com.wayn.domain.api.promotion.service.ShopMemberCouponService;
import com.wayn.domain.trade.support.order.submit.chain.OrderSubmitChain;
import com.wayn.domain.trade.support.order.submit.chain.OrderSubmitChainContext;
import com.wayn.domain.api.trade.request.OrderCommitReqVO;
import com.wayn.domain.api.trade.response.SubmitOrderResVO;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.OrderSnGenUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderSubmitSupportTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(Order.class);
    }

    @Mock
    private RedisCache redisCache;
    @Mock
    private IAddressService addressService;
    @Mock
    private ICartService cartService;
    @Mock
    private OrderSnGenUtil orderSnGenUtil;
    @Mock
    private ShopMemberCouponService shopMemberCouponService;
    @Mock
    private OrderValidationSupport orderValidationSupport;
    @Mock
    private OrderSubmitPersistenceSupport orderSubmitPersistenceSupport;
    @Mock
    private OrderSubmitMessageSupport orderSubmitMessageSupport;
    @Mock
    private OrderSubmitChain orderSubmitChain;
    @InjectMocks
    private OrderSubmitSupport orderSubmitSupport;

    @Test
    void asyncSubmitRejectsMissingCartIds() {
        OrderCommitReqVO reqVO = new OrderCommitReqVO();
        reqVO.setAddressId(1L);
        reqVO.setCartIdArr(List.of(10L, 11L));
        Address address = new Address();
        address.setId(1L);

        when(addressService.getById(1L)).thenReturn(address);
        doNothing().when(orderValidationSupport).validateAddressOwner(address, 100L);
        when(cartService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<Cart>>any()))
                .thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderSubmitSupport.asyncSubmit(reqVO, 100L));

        assertEquals(ReturnCodeEnum.ORDER_SUBMIT_ERROR.getCode(), exception.getCode());
        assertEquals("部分下单商品不存在或已失效", exception.getMsg());
        verify(orderSubmitMessageSupport, never()).sendSubmitMessage(any());
    }

    @Test
    void asyncSubmitReturnsExistingOrderSnWhenSameSubmitFingerprintExists() {
        new WaynConfig().setFreightLimit(new BigDecimal("999999"));
        new WaynConfig().setFreightPrice(BigDecimal.ZERO);
        new WaynConfig().setMobileUrl("http://mobile");
        OrderCommitReqVO reqVO = new OrderCommitReqVO();
        reqVO.setAddressId(1L);
        reqVO.setCartIdArr(List.of(10L));
        Address address = new Address();
        address.setId(1L);
        Cart cart = new Cart();
        cart.setId(10L);
        cart.setGoodsId(20L);
        cart.setProductId(30L);
        cart.setPrice(new BigDecimal("12.50"));
        cart.setNumber(2);

        when(addressService.getById(1L)).thenReturn(address);
        doNothing().when(orderValidationSupport).validateAddressOwner(address, 100L);
        when(cartService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<Cart>>any()))
                .thenReturn(List.of(cart));
        when(orderSnGenUtil.generateOrderSn()).thenReturn("NEW-SN");
        when(redisCache.setCacheObjectIfAbsent(anyString(), eq("NEW-SN"), anyInt())).thenReturn(false);
        when(redisCache.getCacheObject(anyString())).thenReturn("OLD-SN");

        SubmitOrderResVO resVO = orderSubmitSupport.asyncSubmit(reqVO, 100L);

        assertEquals("OLD-SN", resVO.getOrderSn());
        verify(orderSubmitMessageSupport, never()).sendSubmitMessage(any());
    }

    @Test
    void submitExecutesSingleOrderChain() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderSn("ORDER-SN");
        orderDTO.setUserId(100L);
        when(orderSubmitChain.execute(any(OrderSubmitChainContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> orderSubmitSupport.submit(orderDTO));

        verify(orderSubmitChain).execute(any(OrderSubmitChainContext.class));
    }
}
