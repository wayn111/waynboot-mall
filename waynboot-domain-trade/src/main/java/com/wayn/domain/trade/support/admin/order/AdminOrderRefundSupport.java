package com.wayn.domain.trade.support.admin.order;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.entity.OrderGoods;
import com.wayn.domain.api.trade.enums.OrderStatusChangeTypeEnum;
import com.wayn.domain.api.trade.mapper.AdminOrderMapper;
import com.wayn.domain.api.trade.service.IOrderGoodsService;
import com.wayn.domain.api.trade.service.OrderStatusChangeCommand;
import com.wayn.domain.api.trade.service.OrderStatusLogService;
import com.wayn.domain.trade.support.order.OrderStateTransitionSupport;
import com.wayn.domain.inventory.support.OrderStockSupport;
import com.wayn.common.design.strategy.refund.context.RefundContext;
import com.wayn.common.design.strategy.refund.strategy.RefundInterface;
import com.wayn.domain.api.trade.request.OrderRefundReqVO;
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
import com.wayn.domain.inventory.support.OrderStockSupport;
import com.wayn.domain.inventory.support.RedisStockPreDeductSupport;
import com.wayn.domain.inventory.support.RedisStockSnapshotSupport;
import com.wayn.domain.inventory.support.RedisStockBucketRouter;
import com.wayn.domain.inventory.support.RedisStockKeySupport;
import com.wayn.domain.inventory.support.RedisStockReservation;

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
    private final OrderStatusLogService orderStatusLogService;

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
        Order order = validateRefundableOrder(orderSn, refundMoney);
        List<OrderGoods> orderGoodsList = listOrderGoods(order.getId());
        RefundExecution execution = executeRefund(order, reqVO, refundMoney);

        TransactionStatus transaction = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            persistRefundResult(order, orderGoodsList, execution);
            platformTransactionManager.commit(transaction);
        } catch (Exception e) {
            platformTransactionManager.rollback(transaction);
            throw e;
        }
    }

    /**
     * 校验订单是否允许管理端退款。
     * 这里集中处理订单存在性、退款金额和状态机约束，后续流程只关注退款执行和结果落库。
     *
     * @param orderSn 订单号
     * @param refundMoney 退款金额
     * @return 可退款订单
     */
    private Order validateRefundableOrder(String orderSn, BigDecimal refundMoney) {
        Order order = getByOrderSn(orderSn);
        if (order == null) {
            throw new BusinessException(ReturnCodeEnum.ORDER_NOT_FOUND);
        }
        if (refundMoney.compareTo(order.getActualPrice()) > 0) {
            throw new BusinessException(ReturnCodeEnum.ORDER_REFUND_MONEY_LARGE);
        }
        orderStateTransitionSupport.validateTransition(order.getOrderStatus(), OrderStatusEnum.STATUS_REFUND_CONFIRM,
                ReturnCodeEnum.ORDER_CANNOT_REFUND_ERROR);
        return order;
    }

    /**
     * 查询订单明细。
     * 退款成功后需要按订单明细回补库存，查询和事务内回补拆开，避免主流程混入 MyBatis 查询细节。
     *
     * @param orderId 订单 ID
     * @return 订单商品明细
     */
    private List<OrderGoods> listOrderGoods(Long orderId) {
        return orderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_id", orderId));
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
     * 在本地事务内持久化退款结果并处理库存回补。
     * 条件更新要求订单仍处于待退款确认前置状态，避免并发退款或状态漂移覆盖最终状态。
     *
     * @param order 原订单
     * @param orderGoodsList 订单商品明细
     * @param execution 退款执行结果
     */
    private void persistRefundResult(Order order, List<OrderGoods> orderGoodsList, RefundExecution execution) {
        Order update = buildRefundUpdate(order, execution);
        var updateWrapper = Wrappers.lambdaUpdate(Order.class)
                .eq(Order::getId, order.getId());
        orderStateTransitionSupport.applyExpectedStatus(updateWrapper, OrderStatusEnum.STATUS_REFUND);
        int updated = adminOrderMapper.update(update, updateWrapper);
        if (updated == 0) {
            orderStatusLogService.recordFailure(buildRefundStatusLogCommand(order, execution),
                    "订单状态条件更新失败");
            throw new BusinessException(ReturnCodeEnum.ORDER_CANNOT_REFUND_ERROR);
        }
        recordRefundStatusLog(order, execution);
        restoreStockWhenRefundSucceeded(orderGoodsList, execution);
    }

    /**
     * 退款成功后回补库存。
     * 只有三方退款确认成功时才回补库存，避免退款失败时把库存错误加回。
     *
     * @param orderGoodsList 订单商品明细
     * @param execution 退款执行结果
     */
    private void restoreStockWhenRefundSucceeded(List<OrderGoods> orderGoodsList, RefundExecution execution) {
        if (!execution.success()) {
            return;
        }
        orderStockSupport.restoreStock(orderGoodsList);
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
     * 记录管理端退款状态日志。
     *
     * @param order 原订单
     * @param execution 退款执行结果
     */
    private void recordRefundStatusLog(Order order, RefundExecution execution) {
        OrderStatusChangeCommand command = buildRefundStatusLogCommand(order, execution);
        if (execution.success()) {
            orderStatusLogService.recordSuccess(command);
            return;
        }
        orderStatusLogService.recordFailure(command, execution.refundContent());
    }

    /**
     * 构建管理端退款状态日志命令。
     *
     * @param order 原订单
     * @param execution 退款执行结果
     * @return 状态日志命令
     */
    private OrderStatusChangeCommand buildRefundStatusLogCommand(Order order, RefundExecution execution) {
        return OrderStatusChangeCommand.builder()
                .orderId(order.getId())
                .orderSn(order.getOrderSn())
                .sourceStatus(orderStateTransitionSupport.resolve(order.getOrderStatus()))
                .targetStatus(orderStateTransitionSupport.resolve(execution.orderStatus()))
                .changeType(OrderStatusChangeTypeEnum.ADMIN_REFUND)
                .operatorType("ADMIN")
                .operatorId("admin")
                .remark("管理端确认退款")
                .build();
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
