package com.wayn.domain.promotion.support.coupon;

import com.wayn.domain.api.promotion.entity.ShopCoupon;
import com.wayn.domain.api.promotion.entity.ShopMemberCoupon;
import com.wayn.domain.api.promotion.mapper.ShopCouponMapper;
import com.wayn.domain.api.promotion.service.ShopMemberCouponService;
import com.wayn.domain.api.common.TradeLockSupport;
import com.wayn.domain.api.promotion.request.CouponReceiveReqVO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponAcquireSupportTest {

    @Mock
    private ShopCouponMapper shopCouponMapper;
    @Mock
    private ShopMemberCouponService shopMemberCouponService;
    @Mock
    private TradeLockSupport tradeLockSupport;
    @Mock
    private ShopMemberCouponAssembler shopMemberCouponAssembler;
    @InjectMocks
    private CouponAcquireSupport couponAcquireSupport;

    @Test
    void receiveThrowsWhenCouponIsSoldOutDuringConditionalUpdate() {
        CouponReceiveReqVO reqVO = new CouponReceiveReqVO();
        reqVO.setCouponId(1);
        ShopCoupon shopCoupon = new ShopCoupon();
        shopCoupon.setId(1);
        shopCoupon.setStatus(1);
        shopCoupon.setExpireTime(new Date(System.currentTimeMillis() + 60_000));

        when(shopCouponMapper.selectById(1)).thenReturn(shopCoupon);
        when(shopMemberCouponService.count(any())).thenReturn(0L);
        when(shopCouponMapper.updateReceiveNum(1)).thenReturn(0);
        when(tradeLockSupport.executeWithLock(anyString(), eq(2), any(), any()))
                .thenAnswer(invocation -> invocation.<java.util.function.Supplier<Boolean>>getArgument(3).get());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> couponAcquireSupport.receive(reqVO, 100L));

        assertEquals(ReturnCodeEnum.ERROR.getCode(), exception.getCode());
        assertEquals("优惠券已经领完啦", exception.getMsg());
        verify(tradeLockSupport).executeWithLock(anyString(), eq(2), any(), any());
    }
}
