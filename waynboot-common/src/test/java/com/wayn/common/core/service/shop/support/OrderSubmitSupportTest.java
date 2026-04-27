package com.wayn.common.core.service.shop.support;

import com.wayn.common.config.WaynConfig;
import com.wayn.common.core.entity.shop.Address;
import com.wayn.common.core.entity.shop.Cart;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.service.shop.IAddressService;
import com.wayn.common.core.service.shop.ICartService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.core.service.shop.ShopMemberCouponService;
import com.wayn.common.model.request.OrderCommitReqVO;
import com.wayn.common.model.response.SubmitOrderResVO;
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
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
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
    private IOrderGoodsService orderGoodsService;
    @Mock
    private com.wayn.common.core.mapper.shop.OrderMapper orderMapper;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private RabbitTemplate delayRabbitTemplate;
    @Mock
    private OrderSnGenUtil orderSnGenUtil;
    @Mock
    private ShopMemberCouponService shopMemberCouponService;
    @Mock
    private OrderValidationSupport orderValidationSupport;
    @Mock
    private OrderStockSupport orderStockSupport;
    @Mock
    private OrderAssemblerSupport orderAssemblerSupport;
    @Mock
    private TradeLockSupport tradeLockSupport;
    @Mock
    private TransactionTemplate transactionTemplate;
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
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
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
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
    }

    @Test
    void submitSkipsWhenOrderSnAlreadyExists() {
        lenient().doAnswer(invocation -> {
            invocation.<Runnable>getArgument(3).run();
            return null;
        }).when(tradeLockSupport).runWithLock(anyString(), isNull(), any(), any());
        lenient().doAnswer(invocation -> {
            invocation.<Consumer<TransactionStatus>>getArgument(0).accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderSn("DUPLICATE-SN");
        orderDTO.setUserId(100L);
        Order existingOrder = new Order();
        existingOrder.setId(1L);
        existingOrder.setOrderSn("DUPLICATE-SN");

        when(orderMapper.selectOne(any())).thenReturn(existingOrder);

        assertDoesNotThrow(() -> orderSubmitSupport.submit(orderDTO));

        verify(orderStockSupport, never()).reduceStock(any());
        verify(orderMapper, never()).insert(any());
        verify(orderGoodsService, never()).saveBatch(any());
    }
}
