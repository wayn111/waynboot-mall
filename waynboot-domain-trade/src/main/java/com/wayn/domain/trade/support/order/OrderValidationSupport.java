package com.wayn.domain.trade.support.order;

import cn.hutool.core.date.DateUtil;
import com.wayn.domain.api.trade.entity.Address;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.promotion.entity.ShopMemberCoupon;
import com.wayn.domain.api.trade.response.OrderHandleOption;
import com.wayn.common.util.OrderUtil;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 订单校验支撑服务。
 * 统一封装订单、地址、优惠券和操作权限相关的前置校验，避免这些规则散落在支付、下单、取消等流程里。
 */
@Service
public class OrderValidationSupport {

    /**
     * 校验订单是否存在，供旧接口保持原有返回码语义。
     *
     * @param order 订单对象
     * @return 校验结果码
     */
    public ReturnCodeEnum checkOrderOperator(Order order) {
        return order == null ? ReturnCodeEnum.ORDER_NOT_EXISTS_ERROR : ReturnCodeEnum.SUCCESS;
    }

    /**
     * 强制要求订单存在。
     *
     * @param order 订单对象
     * @return 订单对象
     */
    public Order requireOrder(Order order) {
        if (order == null) {
            throw new BusinessException(ReturnCodeEnum.ORDER_NOT_EXISTS_ERROR);
        }
        return order;
    }

    /**
     * 校验地址归属必须属于当前用户。
     *
     * @param address 地址信息
     * @param userId 用户 ID
     */
    public void validateAddressOwner(Address address, Long userId) {
        if (address == null || !Objects.equals(address.getMemberId(), userId)) {
            throw new BusinessException(ReturnCodeEnum.ORDER_ERROR_ADDRESS_ERROR);
        }
    }

    /**
     * 校验优惠券可用性。
     *
     * @param memberCoupon 用户优惠券
     * @param userId 用户 ID
     * @param orderTotalPrice 订单总价
     * @return 可用优惠券
     */
    public ShopMemberCoupon validateCoupon(ShopMemberCoupon memberCoupon, Long userId, BigDecimal orderTotalPrice) {
        if (memberCoupon == null || memberCoupon.getUserId() == null || !Objects.equals(memberCoupon.getUserId().longValue(), userId)) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "优惠卷错误");
        }
        if (!Objects.equals(memberCoupon.getUseStatus(), 0) || DateUtil.compare(memberCoupon.getExpireTime(), new Date()) < 0) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "优惠卷不可用");
        }
        if (memberCoupon.getMin() != null && BigDecimal.valueOf(memberCoupon.getMin()).compareTo(orderTotalPrice) > 0) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "优惠卷使用门槛未达到");
        }
        return memberCoupon;
    }

    /**
     * 校验订单可支付。
     *
     * @param order 订单对象
     */
    public void ensurePayable(Order order) {
        ensureHandleOption(order, OrderHandleOption::isPay, ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
    }

    /**
     * 校验订单可退款。
     *
     * @param order 订单对象
     */
    public void ensureRefundable(Order order) {
        ensureHandleOption(order, OrderHandleOption::isRefund, ReturnCodeEnum.ORDER_CANNOT_REFUND_ERROR);
    }

    /**
     * 校验订单可删除。
     *
     * @param order 订单对象
     */
    public void ensureDeletable(Order order) {
        ensureHandleOption(order, OrderHandleOption::isDelete, ReturnCodeEnum.ORDER_CANNOT_DELETE_ERROR);
    }

    /**
     * 校验订单可确认收货。
     *
     * @param order 订单对象
     */
    public void ensureConfirmable(Order order) {
        ensureHandleOption(order, OrderHandleOption::isConfirm, ReturnCodeEnum.ORDER_CANNOT_CONFIRM_ERROR);
    }

    /**
     * 统一按订单操作权限规则校验当前动作是否允许执行。
     *
     * @param order 订单对象
     * @param predicate 权限断言
     * @param errorCode 失败返回码
     */
    private void ensureHandleOption(Order order, Predicate<OrderHandleOption> predicate, ReturnCodeEnum errorCode) {
        requireOrder(order);
        // 统一复用订单操作权限计算，避免不同入口对可支付/可退款状态判断不一致。
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!predicate.test(handleOption)) {
            throw new BusinessException(errorCode);
        }
    }
}
