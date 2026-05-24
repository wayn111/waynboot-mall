package com.wayn.domain.trade.support.order;

import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.promotion.entity.ShopMemberCoupon;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderValidationSupportTest {

    private final OrderValidationSupport support = new OrderValidationSupport();

    @Test
    void checkOrderOperatorReturnsOrderNotExistsWhenOrderMissing() {
        assertEquals(ReturnCodeEnum.ORDER_NOT_EXISTS_ERROR, support.checkOrderOperator(null));
    }

    @Test
    void validateCouponRejectsWrongOwner() {
        ShopMemberCoupon coupon = new ShopMemberCoupon();
        coupon.setUserId(2);
        coupon.setUseStatus(0);
        coupon.setMin(1);
        coupon.setExpireTime(new Date(System.currentTimeMillis() + 60_000));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> support.validateCoupon(coupon, 1L, BigDecimal.TEN));

        assertEquals(ReturnCodeEnum.ORDER_SUBMIT_ERROR.getCode(), exception.getCode());
        assertEquals("优惠卷错误", exception.getMsg());
    }

    @Test
    void ensurePayableRejectsPaidOrder() {
        Order order = new Order();
        order.setOrderStatus(OrderStatusEnum.STATUS_PAY.getStatus());

        BusinessException exception = assertThrows(BusinessException.class, () -> support.ensurePayable(order));

        assertEquals(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR.getCode(), exception.getCode());
    }
}
