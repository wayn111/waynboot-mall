package com.wayn.domain.trade.support.order;

import com.wayn.domain.api.trade.entity.Address;
import com.wayn.domain.api.cart.entity.Cart;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.entity.OrderGoods;
import com.wayn.domain.api.trade.mapper.OrderMapper;
import com.wayn.domain.api.cart.service.ICartService;
import com.wayn.domain.api.trade.service.IOrderGoodsService;
import com.wayn.domain.api.promotion.service.ShopMemberCouponService;
import com.wayn.domain.api.common.MybatisPlusTableInfoTestHelper;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderSubmitPersistenceSupportTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(Cart.class);
        MybatisPlusTableInfoTestHelper.init(Order.class);
    }

    @Test
    void persistSingleFailsWhenOrderInsertFailed() {
        ICartService cartService = mock(ICartService.class);
        IOrderGoodsService orderGoodsService = mock(IOrderGoodsService.class);
        ShopMemberCouponService shopMemberCouponService = mock(ShopMemberCouponService.class);
        OrderMapper orderMapper = mock(OrderMapper.class);
        OrderAssemblerSupport orderAssemblerSupport = mock(OrderAssemblerSupport.class);
        OrderSubmitPersistenceSupport support = new OrderSubmitPersistenceSupport(
                cartService, orderGoodsService, shopMemberCouponService, orderMapper, orderAssemblerSupport);
        OrderDTO orderDTO = buildOrderDTO();
        Order order = new Order();
        when(orderMapper.insert(order)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> support.persistSingle(orderDTO, buildContext(), order));

        assertEquals(ReturnCodeEnum.ORDER_SUBMIT_ERROR.getCode(), exception.getCode());
    }

    @Test
    void persistSingleWritesOrderGoodsAndClearsSubmittedCart() {
        ICartService cartService = mock(ICartService.class);
        IOrderGoodsService orderGoodsService = mock(IOrderGoodsService.class);
        ShopMemberCouponService shopMemberCouponService = mock(ShopMemberCouponService.class);
        OrderMapper orderMapper = mock(OrderMapper.class);
        OrderAssemblerSupport orderAssemblerSupport = mock(OrderAssemblerSupport.class);
        OrderSubmitPersistenceSupport support = new OrderSubmitPersistenceSupport(
                cartService, orderGoodsService, shopMemberCouponService, orderMapper, orderAssemblerSupport);
        OrderDTO orderDTO = buildOrderDTO();
        Order order = new Order();
        order.setId(200L);
        OrderSubmitContext context = buildContext();
        List<OrderGoods> orderGoodsList = List.of(new OrderGoods());
        when(orderMapper.insert(order)).thenReturn(1);
        when(orderAssemblerSupport.buildOrderGoods(200L, context.checkedGoodsList())).thenReturn(orderGoodsList);
        when(orderGoodsService.saveBatch(orderGoodsList)).thenReturn(true);

        support.persistSingle(orderDTO, context, order);

        verify(orderMapper).insert(order);
        verify(orderGoodsService).saveBatch(orderGoodsList);
        verify(cartService).remove(any());
    }

    private OrderDTO buildOrderDTO() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderSn("ORDER-OK");
        orderDTO.setUserId(100L);
        orderDTO.setCartIdArr(List.of(10L));
        return orderDTO;
    }

    private OrderSubmitContext buildContext() {
        Address address = new Address();
        address.setId(1L);
        Cart cart = new Cart();
        cart.setId(10L);
        cart.setGoodsId(20L);
        cart.setProductId(30L);
        cart.setPrice(new BigDecimal("10.00"));
        cart.setNumber(1);
        return new OrderSubmitContext(address, List.of(cart), new BigDecimal("10.00"), BigDecimal.ZERO,
                new BigDecimal("10.00"), BigDecimal.ZERO, new BigDecimal("10.00"));
    }
}
