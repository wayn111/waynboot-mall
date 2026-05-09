package com.wayn.common.core.service.shop.support.admin.order;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.mapper.shop.AdminOrderMapper;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.core.service.shop.support.order.OrderStateTransitionSupport;
import com.wayn.common.core.service.shop.support.order.OrderStockSupport;
import com.wayn.common.design.strategy.refund.context.RefundContext;
import com.wayn.common.design.strategy.refund.strategy.RefundInterface;
import com.wayn.common.model.request.OrderRefundReqVO;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.RefundStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 管理端订单退款支撑服务。
 * 负责退款前校验、三方退款调用、退款结果落库和库存回补，并通过条件更新避免重复退款覆盖状态。
 */
@Slf4j
@Service
@AllArgsConstructor
public class AdminOrderRefundSupport {

    private final AdminOrderMapper adminOrderMapper;
    private final IOrderGoodsService orderGoodsService;
    private final OrderStockSupport orderStockSupport;
    private final RefundContext refundContext;
    private final PlatformTransactionManager platformTransactionManager;
    private final OrderStateTransitionSupport orderStateTransitionSupport;

    /**
     * 执行管理端订单退款。
     * 该方法会先完成退款前校验，再调用三方退款，最后通过条件更新落库退款结果并在成功时回补库存。
     *
     * @param reqVO 退款请求
     * @throws UnsupportedEncodingException 编码异常
     * @throws WxPayException 微信退款异常
     * @throws AlipayApiException 支付宝退款异常
     */
    public void refund(OrderRefundReqVO reqVO) throws UnsupportedEncodingException, WxPayException, AlipayApiException {
        String orderSn = reqVO.getOrderSn();
        BigDecimal refundMoney = reqVO.getRefundMoney();
        Order order = getByOrderSn(orderSn);
        if (order == null) {
            throw new BusinessException(ReturnCodeEnum.ORDER_NOT_FOUND);
        }
        if (refundMoney.compareTo(order.getActualPrice()) > 0) {
            throw new BusinessException(ReturnCodeEnum.ORDER_REFUND_MONEY_LARGE);
        }
        orderStateTransitionSupport.validateTransition(order.getOrderStatus(), OrderStatusEnum.STATUS_REFUND_CONFIRM,
                ReturnCodeEnum.ORDER_CANNOT_REFUND_ERROR);
        List<OrderGoods> orderGoodsList = orderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_id", order.getId()));

        RefundExecution execution = executeRefund(order, reqVO, refundMoney);
        TransactionStatus transaction = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Order update = buildRefundUpdate(order, execution);
            int updated = adminOrderMapper.update(update, Wrappers.lambdaUpdate(Order.class)
                    .eq(Order::getId, order.getId())
                    .eq(Order::getOrderStatus, OrderStatusEnum.STATUS_REFUND.getStatus()));
            if (updated == 0) {
                throw new BusinessException(ReturnCodeEnum.ORDER_CANNOT_REFUND_ERROR);
            }
            // 只有三方退款确认成功时才回补库存，避免退款失败时把库存错误加回。
            if (execution.success()) {
                orderStockSupport.restoreStock(orderGoodsList);
            }
            platformTransactionManager.commit(transaction);
        } catch (RuntimeException e) {
            platformTransactionManager.rollback(transaction);
            throw e;
        } catch (Error e) {
            platformTransactionManager.rollback(transaction);
            throw e;
        }
    }

    /**
     * 调用三方退款并把结果收敛为统一结构。
     * 三方退款失败时不直接抛出给上层事务，而是转成退款失败状态，由事务阶段统一落库。
     *
     * @param order 订单信息
     * @param reqVO 退款请求
     * @param refundMoney 退款金额
     * @return 退款执行结果
     * @throws UnsupportedEncodingException 编码异常
     * @throws WxPayException 微信退款异常
     * @throws AlipayApiException 支付宝退款异常
     */
    private RefundExecution executeRefund(Order order, OrderRefundReqVO reqVO, BigDecimal refundMoney)
            throws UnsupportedEncodingException, WxPayException, AlipayApiException {
        try {
            RefundInterface instance = refundContext.getInstance(order.getPayType());
            reqVO.setPayId(order.getPayId());
            instance.refund(reqVO);
            return new RefundExecution(true, OrderStatusEnum.STATUS_REFUND_CONFIRM.getStatus(),
                    RefundStatusEnum.REFUND_SUCCESS.getStatus(), refundMoney, reqVO.getRefundReason());
        } catch (Exception e) {
            log.error("订单退款失败, orderSn={}", order.getOrderSn(), e);
            String refundReason = reqVO.getRefundReason() + " 退款失败：" + StringUtils.substring(e.getMessage(), 0, 2000);
            return new RefundExecution(false, OrderStatusEnum.STATUS_REFUND.getStatus(),
                    RefundStatusEnum.REFUND_FAIL.getStatus(), BigDecimal.ZERO, refundReason);
        }
    }

    /**
     * 构建订单退款更新对象。
     *
     * @param order 原订单
     * @param execution 退款执行结果
     * @return 待更新订单对象
     */
    private Order buildRefundUpdate(Order order, RefundExecution execution) {
        Order update = new Order();
        LocalDateTime now = LocalDateTime.now();
        update.setOrderStatus(execution.orderStatus());
        update.setOrderEndTime(now);
        update.setRefundStatus(execution.refundStatus());
        update.setRefundAmount(execution.refundAmount());
        update.setRefundType(order.getPayType());
        update.setRefundContent(execution.refundContent());
        update.setRefundTime(now);
        update.setUpdateTime(new Date());
        return update;
    }

    /**
     * 根据订单号查询订单。
     *
     * @param orderSn 订单号
     * @return 订单信息
     */
    private Order getByOrderSn(String orderSn) {
        return adminOrderMapper.selectOne(Wrappers.lambdaQuery(Order.class).eq(Order::getOrderSn, orderSn));
    }

    /**
     * 退款执行结果。
     * 用于在三方退款阶段和数据库事务阶段之间传递状态，避免分散维护多个临时变量。
     */
    private record RefundExecution(boolean success, Short orderStatus, Integer refundStatus, BigDecimal refundAmount,
                                   String refundContent) {
    }
}
