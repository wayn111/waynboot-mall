package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.PaymentChannelBill;
import com.wayn.common.core.entity.shop.PaymentFlow;
import com.wayn.common.core.entity.shop.PaymentRefundFlow;
import com.wayn.common.core.mapper.shop.OrderMapper;
import com.wayn.common.core.mapper.shop.PaymentChannelBillMapper;
import com.wayn.common.core.mapper.shop.PaymentFlowMapper;
import com.wayn.common.core.mapper.shop.PaymentRefundFlowMapper;
import com.wayn.util.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * 支付对账服务。
 * 基于支付流水、渠道账单、退款流水和订单表识别支付链路差异。
 */
@Service
@AllArgsConstructor
public class PaymentReconciliationService {

    private static final String DIFF_FLOW_WITHOUT_ORDER = "FLOW_WITHOUT_ORDER";
    private static final String DIFF_ORDER_STATUS_MISMATCH = "ORDER_STATUS_MISMATCH";
    private static final String DIFF_AMOUNT_MISMATCH = "AMOUNT_MISMATCH";
    private static final String DIFF_ORDER_WITHOUT_FLOW = "ORDER_WITHOUT_FLOW";
    private static final String DIFF_CHANNEL_BILL_WITHOUT_FLOW = "CHANNEL_BILL_WITHOUT_FLOW";
    private static final String DIFF_CHANNEL_AMOUNT_MISMATCH = "CHANNEL_AMOUNT_MISMATCH";
    private static final String DIFF_REFUND_FLOW_WITHOUT_ORDER = "REFUND_FLOW_WITHOUT_ORDER";
    private static final String DIFF_REFUND_AMOUNT_MISMATCH = "REFUND_AMOUNT_MISMATCH";
    private static final String MSG_FLOW_WITHOUT_ORDER = "支付流水找不到订单";
    private static final String MSG_ORDER_STATUS_MISMATCH = "订单不是已支付状态";
    private static final String MSG_AMOUNT_MISMATCH = "支付流水金额和订单实付金额不一致";
    private static final String MSG_ORDER_WITHOUT_FLOW = "已支付订单缺少支付流水";
    private static final String MSG_CHANNEL_BILL_WITHOUT_FLOW = "渠道账单缺少内部支付流水";
    private static final String MSG_CHANNEL_AMOUNT_MISMATCH = "渠道账单金额和内部支付流水金额不一致";
    private static final String MSG_REFUND_FLOW_WITHOUT_ORDER = "退款流水找不到订单";
    private static final String MSG_REFUND_AMOUNT_MISMATCH = "退款流水金额和订单退款金额不一致";

    private final PaymentFlowMapper paymentFlowMapper;
    private final OrderMapper orderMapper;
    private final PaymentChannelBillMapper paymentChannelBillMapper;
    private final PaymentRefundFlowMapper paymentRefundFlowMapper;

    /**
     * 执行支付流水、渠道账单、退款流水与订单的基础对账。
     *
     * @param query 对账查询条件
     * @return 对账汇总
     */
    public PaymentReconciliationSummary reconcile(PaymentReconciliationQuery query) {
        PaymentReconciliationQuery safeQuery = query == null ? PaymentReconciliationQuery.defaultQuery() : query;
        PaymentReconciliationSummary summary = new PaymentReconciliationSummary();
        List<PaymentFlow> paymentFlows = listPaymentFlows(safeQuery);
        summary.setPaymentFlowCount(paymentFlows.size());
        Set<String> paymentFlowOrderSnSet = collectPaymentFlowOrderSnSet(paymentFlows);
        Map<String, Order> paymentFlowOrderMap = loadOrdersByOrderSn(paymentFlowOrderSnSet);
        for (PaymentFlow paymentFlow : paymentFlows) {
            reconcilePaymentFlow(summary, paymentFlow, paymentFlowOrderMap.get(paymentFlow.getOrderSn()));
        }

        List<Order> paidOrders = listPaidOrders(safeQuery);
        summary.setPaidOrderCount(paidOrders.size());
        for (Order order : paidOrders) {
            reconcilePaidOrder(summary, order, paymentFlowOrderSnSet);
        }
        reconcileChannelBills(summary, safeQuery);
        reconcileRefundFlows(summary, safeQuery);
        return summary;
    }

    /**
     * 对账支付流水维度。
     * 支付流水来自内部支付回调，是订单已支付状态的内部证据；该维度主要发现“有流水无订单”、
     * “流水订单状态不对”和“金额不一致”。
     *
     * @param summary 对账汇总
     * @param paymentFlow 支付流水
     * @param order 支付流水关联的订单，前置批量加载后传入，避免对账扫描产生 N+1 查询
     */
    private void reconcilePaymentFlow(PaymentReconciliationSummary summary, PaymentFlow paymentFlow, Order order) {
        if (order == null) {
            summary.addMissingOrder(buildDifference(DIFF_FLOW_WITHOUT_ORDER, paymentFlow.getOrderSn(),
                    paymentFlow.getPayAmount(), null, MSG_FLOW_WITHOUT_ORDER));
            return;
        }
        if (!Objects.equals(order.getOrderStatus(), OrderStatusEnum.STATUS_PAY.getStatus())) {
            summary.addOrderStatusMismatch(buildDifference(DIFF_ORDER_STATUS_MISMATCH, paymentFlow.getOrderSn(),
                    paymentFlow.getPayAmount(), order.getActualPrice(), MSG_ORDER_STATUS_MISMATCH));
            return;
        }
        if (!sameAmount(paymentFlow.getPayAmount(), order.getActualPrice())) {
            summary.addAmountMismatch(buildDifference(DIFF_AMOUNT_MISMATCH, paymentFlow.getOrderSn(),
                    paymentFlow.getPayAmount(), order.getActualPrice(), MSG_AMOUNT_MISMATCH));
        }
    }

    /**
     * 收集支付流水中的订单号。
     * 空订单号会被保留为差异来源，但不会参与数据库 IN 查询，避免生成无意义查询条件。
     *
     * @param paymentFlows 支付流水列表
     * @return 非空订单号集合
     */
    private Set<String> collectPaymentFlowOrderSnSet(List<PaymentFlow> paymentFlows) {
        Set<String> orderSnSet = new HashSet<>();
        for (PaymentFlow paymentFlow : paymentFlows) {
            if (paymentFlow != null && paymentFlow.getOrderSn() != null) {
                orderSnSet.add(paymentFlow.getOrderSn());
            }
        }
        return orderSnSet;
    }

    /**
     * 按订单号批量加载订单快照。
     * 支付对账通常按天或按窗口批量扫描，批量加载可以把“流水数 N 次订单查询”收敛为一次 IN 查询。
     *
     * @param orderSnSet 订单号集合
     * @return 订单号到订单的映射
     */
    private Map<String, Order> loadOrdersByOrderSn(Set<String> orderSnSet) {
        if (CollectionUtils.isEmpty(orderSnSet)) {
            return Map.of();
        }
        List<Order> orders = safeRows(orderMapper.selectList(Wrappers.lambdaQuery(Order.class)
                .in(Order::getOrderSn, orderSnSet)));
        return indexOrdersByOrderSn(orders);
    }

    /**
     * 将订单列表按订单号索引。
     * 如果数据库异常返回重复订单号，保留第一条记录，避免后续重复数据覆盖原始对账证据。
     *
     * @param orders 订单列表
     * @return 订单号到订单的映射
     */
    private Map<String, Order> indexOrdersByOrderSn(List<Order> orders) {
        Map<String, Order> orderMap = new HashMap<>();
        for (Order order : orders) {
            if (order != null && order.getOrderSn() != null) {
                orderMap.putIfAbsent(order.getOrderSn(), order);
            }
        }
        return orderMap;
    }

    /**
     * 对账已支付订单维度。
     * 已支付订单必须能反查到内部支付流水，否则说明支付回调落流水或补偿链路存在缺口。
     *
     * @param summary 对账汇总
     * @param order 已支付订单
     */
    private void reconcilePaidOrder(PaymentReconciliationSummary summary, Order order, Set<String> paymentFlowOrderSnSet) {
        if (paymentFlowOrderSnSet.contains(order.getOrderSn())) {
            return;
        }
        PaymentFlow paymentFlow = findPaymentFlowByOrderSn(order.getOrderSn());
        if (paymentFlow == null) {
            summary.addMissingPaymentFlow(buildDifference(DIFF_ORDER_WITHOUT_FLOW, order.getOrderSn(),
                    null, order.getActualPrice(), MSG_ORDER_WITHOUT_FLOW));
        }
    }

    /**
     * 对账渠道账单维度。
     * 渠道账单是支付平台侧的资金证据，和内部流水比对可以发现回调丢失或渠道金额差异。
     *
     * @param summary 对账汇总
     * @param query 对账查询条件
     */
    private void reconcileChannelBills(PaymentReconciliationSummary summary, PaymentReconciliationQuery query) {
        List<PaymentChannelBill> channelBills = listChannelBills(query);
        summary.setChannelBillCount(channelBills.size());
        Map<PaymentIdentity, PaymentFlow> paymentFlowMap = loadPaymentFlowsByIdentity(collectPaymentIdentities(channelBills));
        for (PaymentChannelBill channelBill : channelBills) {
            PaymentFlow paymentFlow = paymentFlowMap.get(PaymentIdentity.of(channelBill));
            if (paymentFlow == null) {
                summary.addChannelBillMismatch(buildDifference(DIFF_CHANNEL_BILL_WITHOUT_FLOW,
                        channelBill.getOrderSn(), channelBill.getPayAmount(), null, MSG_CHANNEL_BILL_WITHOUT_FLOW));
                continue;
            }
            if (!sameAmount(channelBill.getPayAmount(), paymentFlow.getPayAmount())) {
                summary.addChannelBillMismatch(buildDifference(DIFF_CHANNEL_AMOUNT_MISMATCH,
                        channelBill.getOrderSn(), channelBill.getPayAmount(), paymentFlow.getPayAmount(),
                        MSG_CHANNEL_AMOUNT_MISMATCH));
            }
        }
    }

    /**
     * 对账退款流水维度。
     * 退款流水和订单退款金额保持独立校验，避免支付成功链路和退款链路互相掩盖异常。
     *
     * @param summary 对账汇总
     * @param query 对账查询条件
     */
    private void reconcileRefundFlows(PaymentReconciliationSummary summary, PaymentReconciliationQuery query) {
        List<PaymentRefundFlow> refundFlows = listRefundFlows(query);
        summary.setRefundFlowCount(refundFlows.size());
        Map<String, Order> refundOrderMap = loadOrdersByOrderSn(collectRefundOrderSnSet(refundFlows));
        for (PaymentRefundFlow refundFlow : refundFlows) {
            Order order = refundOrderMap.get(refundFlow.getOrderSn());
            if (order == null) {
                summary.addRefundFlowMismatch(buildDifference(DIFF_REFUND_FLOW_WITHOUT_ORDER,
                        refundFlow.getOrderSn(), refundFlow.getRefundAmount(), null, MSG_REFUND_FLOW_WITHOUT_ORDER));
                continue;
            }
            if (!sameAmount(refundFlow.getRefundAmount(), order.getRefundAmount())) {
                summary.addRefundFlowMismatch(buildDifference(DIFF_REFUND_AMOUNT_MISMATCH,
                        refundFlow.getOrderSn(), refundFlow.getRefundAmount(), order.getRefundAmount(),
                        MSG_REFUND_AMOUNT_MISMATCH));
            }
        }
    }

    /**
     * 收集渠道账单中的支付身份。
     * 支付身份由“渠道 + 第三方流水号”组成，是渠道账单和内部支付流水对齐的稳定键。
     *
     * @param channelBills 渠道账单列表
     * @return 支付身份集合
     */
    private Set<PaymentIdentity> collectPaymentIdentities(List<PaymentChannelBill> channelBills) {
        Set<PaymentIdentity> identities = new HashSet<>();
        for (PaymentChannelBill channelBill : channelBills) {
            PaymentIdentity identity = PaymentIdentity.of(channelBill);
            if (identity.isComplete()) {
                identities.add(identity);
            }
        }
        return identities;
    }

    /**
     * 批量加载渠道账单对应的内部支付流水。
     * MyBatis-Plus LambdaWrapper 不适合直接表达多列 tuple IN，这里先按 payId 集合收敛查询范围，
     * 再在内存中按“渠道 + 流水号”精确匹配，避免为每条账单单独查询。
     *
     * @param identities 支付身份集合
     * @return 支付身份到内部支付流水的映射
     */
    private Map<PaymentIdentity, PaymentFlow> loadPaymentFlowsByIdentity(Set<PaymentIdentity> identities) {
        if (CollectionUtils.isEmpty(identities)) {
            return Map.of();
        }
        Set<String> payIds = new HashSet<>();
        for (PaymentIdentity identity : identities) {
            payIds.add(identity.payId());
        }
        List<PaymentFlow> paymentFlows = safeRows(paymentFlowMapper.selectList(Wrappers.lambdaQuery(PaymentFlow.class)
                .in(PaymentFlow::getPayId, payIds)));
        return indexPaymentFlowsByIdentity(paymentFlows);
    }

    /**
     * 将支付流水按支付身份索引。
     * 如果出现重复支付身份，保留第一条，避免异常数据覆盖最早扫描到的差异证据。
     *
     * @param paymentFlows 支付流水列表
     * @return 支付身份到内部支付流水的映射
     */
    private Map<PaymentIdentity, PaymentFlow> indexPaymentFlowsByIdentity(List<PaymentFlow> paymentFlows) {
        Map<PaymentIdentity, PaymentFlow> paymentFlowMap = new HashMap<>();
        for (PaymentFlow paymentFlow : paymentFlows) {
            PaymentIdentity identity = PaymentIdentity.of(paymentFlow);
            if (identity.isComplete()) {
                paymentFlowMap.putIfAbsent(identity, paymentFlow);
            }
        }
        return paymentFlowMap;
    }

    /**
     * 收集退款流水中的订单号。
     * 空订单号不参与 IN 查询，后续对账分支会自然形成“退款流水找不到订单”差异。
     *
     * @param refundFlows 退款流水列表
     * @return 非空订单号集合
     */
    private Set<String> collectRefundOrderSnSet(List<PaymentRefundFlow> refundFlows) {
        Set<String> orderSnSet = new HashSet<>();
        for (PaymentRefundFlow refundFlow : refundFlows) {
            if (refundFlow != null && refundFlow.getOrderSn() != null) {
                orderSnSet.add(refundFlow.getOrderSn());
            }
        }
        return orderSnSet;
    }

    /**
     * 构建支付对账差异明细。
     * 对账服务内统一创建差异对象，避免各业务分支重复拼装字段导致文案和字段语义漂移。
     *
     * @param differenceType 差异类型
     * @param orderSn 订单号
     * @param flowAmount 流水或渠道侧金额
     * @param orderAmount 订单或内部流水金额
     * @param message 差异说明
     * @return 支付对账差异明细
     */
    private PaymentReconciliationDifference buildDifference(String differenceType, String orderSn,
                                                            BigDecimal flowAmount, BigDecimal orderAmount,
                                                            String message) {
        return new PaymentReconciliationDifference(differenceType, orderSn, flowAmount, orderAmount, message);
    }

    /**
     * 查询支付流水。
     *
     * @param query 对账查询条件
     * @return 支付流水列表
     */
    private List<PaymentFlow> listPaymentFlows(PaymentReconciliationQuery query) {
        LambdaQueryWrapper<PaymentFlow> wrapper = applyTimeRange(Wrappers.lambdaQuery(PaymentFlow.class),
                query, PaymentFlow::getCreateTime);
        return selectLimitedList(wrapper, query, PaymentFlow::getCreateTime, paymentFlowMapper::selectList);
    }

    /**
     * 查询已支付订单。
     *
     * @param query 对账查询条件
     * @return 已支付订单列表
     */
    private List<Order> listPaidOrders(PaymentReconciliationQuery query) {
        LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderStatus, OrderStatusEnum.STATUS_PAY.getStatus());
        applyTimeRange(wrapper, query, Order::getCreateTime);
        return selectLimitedList(wrapper, query, Order::getCreateTime, orderMapper::selectList);
    }

    /**
     * 查询渠道账单。
     *
     * @param query 对账查询条件
     * @return 渠道账单列表
     */
    private List<PaymentChannelBill> listChannelBills(PaymentReconciliationQuery query) {
        LambdaQueryWrapper<PaymentChannelBill> wrapper = applyTimeRange(
                Wrappers.lambdaQuery(PaymentChannelBill.class), query, PaymentChannelBill::getBillDate);
        return selectLimitedList(wrapper, query, PaymentChannelBill::getBillDate,
                paymentChannelBillMapper::selectList);
    }

    /**
     * 查询退款流水。
     *
     * @param query 对账查询条件
     * @return 退款流水列表
     */
    private List<PaymentRefundFlow> listRefundFlows(PaymentReconciliationQuery query) {
        LambdaQueryWrapper<PaymentRefundFlow> wrapper = applyTimeRange(Wrappers.lambdaQuery(PaymentRefundFlow.class),
                query, PaymentRefundFlow::getCreateTime);
        return selectLimitedList(wrapper, query, PaymentRefundFlow::getCreateTime,
                paymentRefundFlowMapper::selectList);
    }

    /**
     * 按订单号查询内部支付流水。
     *
     * @param orderSn 订单号
     * @return 支付流水，不存在时返回 null
     */
    private PaymentFlow findPaymentFlowByOrderSn(String orderSn) {
        return paymentFlowMapper.selectOne(Wrappers.lambdaQuery(PaymentFlow.class)
                .eq(PaymentFlow::getOrderSn, orderSn)
                .last("limit 1"));
    }

    /**
     * 给对账扫描追加时间范围。
     * 支付流水、渠道账单和退款流水的时间字段不同，但窗口语义一致，统一收口避免后续新增条件时遗漏某一类账务数据。
     *
     * @param wrapper 查询包装器
     * @param query 对账查询条件
     * @param timeField 时间字段
     * @param <T> 实体类型
     * @return 已追加时间范围的查询包装器
     */
    private <T> LambdaQueryWrapper<T> applyTimeRange(LambdaQueryWrapper<T> wrapper,
                                                     PaymentReconciliationQuery query,
                                                     SFunction<T, ?> timeField) {
        if (query.startTime() != null) {
            wrapper.ge(timeField, query.startTime());
        }
        if (query.endTime() != null) {
            wrapper.le(timeField, query.endTime());
        }
        return wrapper;
    }

    /**
     * 执行带排序和安全 limit 的扫描查询。
     * 对账任务可能由后台接口或 XXL-Job 触发，limit 必须统一走 safeLimit，避免误传大 limit 造成一次扫描过重。
     *
     * @param wrapper 查询包装器
     * @param query 对账查询条件
     * @param orderField 排序字段
     * @param selector Mapper 查询函数
     * @param <T> 实体类型
     * @return 非空查询结果
     */
    private <T> List<T> selectLimitedList(LambdaQueryWrapper<T> wrapper,
                                          PaymentReconciliationQuery query,
                                          SFunction<T, ?> orderField,
                                          Function<LambdaQueryWrapper<T>, List<T>> selector) {
        wrapper.orderByAsc(orderField).last(limitClause(query));
        return safeRows(selector.apply(wrapper));
    }

    /**
     * 构造安全 limit 片段。
     *
     * @param query 对账查询条件
     * @return MyBatis-Plus last 使用的 limit 片段
     */
    private String limitClause(PaymentReconciliationQuery query) {
        return "limit " + query.safeLimit();
    }

    /**
     * 比较金额是否一致。
     *
     * @param left 左侧金额
     * @param right 右侧金额
     * @return true=金额一致
     */
    private boolean sameAmount(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.compareTo(right) == 0;
    }

    /**
     * 将 Mapper 查询结果转成可安全遍历的行集合。
     * 对账扫描来自定时任务或后台运营入口，单条空行不应中断整批扫描；这里统一过滤 null，
     * 避免支付流水、渠道账单、退款流水等每个循环都重复写空值分支。
     *
     * @param list 原始列表
     * @param <T> 列表元素类型
     * @return 非空且不包含 null 元素的列表
     */
    private <T> List<T> safeRows(List<T> list) {
        return CollectionUtils.emptyIfNull(list).stream()
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 支付渠道身份键。
     * 对账时渠道账单和内部流水都可能包含同一第三方流水号，必须叠加支付渠道才能避免跨渠道误匹配。
     *
     * @param payId 第三方支付流水号
     * @param payChannel 支付渠道
     */
    private record PaymentIdentity(String payId, String payChannel) {

        /**
         * 从渠道账单构建支付身份。
         *
         * @param channelBill 渠道账单
         * @return 支付身份
         */
        private static PaymentIdentity of(PaymentChannelBill channelBill) {
            if (channelBill == null) {
                return new PaymentIdentity(null, null);
            }
            return new PaymentIdentity(channelBill.getPayId(), channelBill.getPayChannel());
        }

        /**
         * 从内部支付流水构建支付身份。
         *
         * @param paymentFlow 支付流水
         * @return 支付身份
         */
        private static PaymentIdentity of(PaymentFlow paymentFlow) {
            if (paymentFlow == null) {
                return new PaymentIdentity(null, null);
            }
            return new PaymentIdentity(paymentFlow.getPayId(), paymentFlow.getPayChannel());
        }

        /**
         * 判断支付身份是否完整。
         * 渠道账单导入可能出现空白支付流水号；这类数据应直接形成对账差异，
         * 不能继续参与批量查询，否则日终对账会产生无效 DB 压力。
         *
         * @return true=支付流水号和渠道均不为空
         */
        private boolean isComplete() {
            return StringUtils.isNoneBlank(payId, payChannel);
        }
    }
}
