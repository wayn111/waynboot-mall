package com.wayn.domain.trade.support.order;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.common.MybatisPlusTableInfoTestHelper;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderStateTransitionSupportTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisPlusTableInfoTestHelper.init(Order.class);
    }

    /**
     * 验证状态机允许配置中的合法流转。
     */
    @Test
    void validateAllowsConfiguredTransition() {
        OrderStateTransitionSupport support = new OrderStateTransitionSupport();

        assertDoesNotThrow(() -> support.validateTransition(OrderStatusEnum.STATUS_CREATE,
                OrderStatusEnum.STATUS_PAY, ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR));
    }

    /**
     * 验证状态机会拒绝未配置的非法流转。
     */
    @Test
    void validateRejectsUnexpectedTransition() {
        OrderStateTransitionSupport support = new OrderStateTransitionSupport();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> support.validateTransition(OrderStatusEnum.STATUS_CANCEL,
                        OrderStatusEnum.STATUS_PAY, ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR));

        assertEquals(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR.getCode(), exception.getCode());
    }

    /**
     * 验证状态机可以给条件更新追加当前状态约束。
     */
    @Test
    void applyExpectedStatusAddsCurrentStatusCondition() {
        OrderStateTransitionSupport support = new OrderStateTransitionSupport();
        var wrapper = Wrappers.lambdaUpdate(Order.class);

        support.applyExpectedStatus(wrapper, OrderStatusEnum.STATUS_CREATE);

        assertEquals("(order_status = #{ew.paramNameValuePairs.MPGENVAL1})",
                wrapper.getExpression().getNormal().getSqlSegment());
    }
}
