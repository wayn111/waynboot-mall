package com.wayn.common.core.service.shop.support.payment;

import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wayn.common.config.AlipayConfig;
import com.wayn.common.config.EpayConfig;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.enums.OrderStatusChangeTypeEnum;
import com.wayn.common.core.enums.PaymentFlowSaveResult;
import com.wayn.common.core.enums.PaymentNotifyChannelEnum;
import com.wayn.common.core.mapper.shop.OrderMapper;
import com.wayn.common.core.service.shop.OrderStatusChangeCommand;
import com.wayn.common.core.service.shop.OrderStatusLogService;
import com.wayn.common.core.service.shop.PaymentFlowCreateCommand;
import com.wayn.common.core.service.shop.PaymentFlowService;
import com.wayn.common.core.service.shop.support.order.OrderStateTransitionSupport;
import com.wayn.common.util.OrderUtil;
import com.wayn.common.wapper.epay.util.EpaySignUtil;
import com.wayn.util.enums.OrderStatusEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付回调支撑服务。
 * 渠道差异只体现在验签和参数解析，订单状态更新、幂等判断和后置动作全部走统一入口。
 */
@Slf4j
@Service
public class PaymentCallbackSupport {

    private final EpayConfig epayConfig;
    private final AlipayConfig alipayConfig;
    private final WxPayService wxPayService;
    private final OrderMapper orderMapper;
    private final PaymentPostActionSupport paymentPostActionSupport;
    private final OrderStateTransitionSupport orderStateTransitionSupport;
    private final PaymentFlowService paymentFlowService;
    private final OrderStatusLogService orderStatusLogService;
    private final TransactionTemplate transactionTemplate;

    /**
     * Spring 运行时构造器。
     *
     * @param epayConfig 易支付配置
     * @param alipayConfig 支付宝配置
     * @param wxPayService 微信支付服务
     * @param orderMapper 订单 Mapper
     * @param paymentPostActionSupport 支付后置动作支撑服务
     * @param orderStateTransitionSupport 订单状态机
     * @param paymentFlowService 支付流水服务
     * @param orderStatusLogService 订单状态日志服务
     * @param platformTransactionManager 事务管理器
     */
    @Autowired
    public PaymentCallbackSupport(EpayConfig epayConfig, AlipayConfig alipayConfig, WxPayService wxPayService,
                                  OrderMapper orderMapper, PaymentPostActionSupport paymentPostActionSupport,
                                  OrderStateTransitionSupport orderStateTransitionSupport,
                                  PaymentFlowService paymentFlowService,
                                  OrderStatusLogService orderStatusLogService,
                                  PlatformTransactionManager platformTransactionManager) {
        this(epayConfig, alipayConfig, wxPayService, orderMapper, paymentPostActionSupport,
                orderStateTransitionSupport, paymentFlowService, orderStatusLogService,
                new TransactionTemplate(platformTransactionManager));
    }

    /**
     * 单元测试构造器。
     *
     * @param epayConfig 易支付配置
     * @param alipayConfig 支付宝配置
     * @param wxPayService 微信支付服务
     * @param orderMapper 订单 Mapper
     * @param paymentPostActionSupport 支付后置动作支撑服务
     * @param orderStateTransitionSupport 订单状态机
     * @param paymentFlowService 支付流水服务
     * @param orderStatusLogService 订单状态日志服务
     */
    public PaymentCallbackSupport(EpayConfig epayConfig, AlipayConfig alipayConfig, WxPayService wxPayService,
                                  OrderMapper orderMapper, PaymentPostActionSupport paymentPostActionSupport,
                                  OrderStateTransitionSupport orderStateTransitionSupport,
                                  PaymentFlowService paymentFlowService,
                                  OrderStatusLogService orderStatusLogService) {
        this(epayConfig, alipayConfig, wxPayService, orderMapper, paymentPostActionSupport,
                orderStateTransitionSupport, paymentFlowService, orderStatusLogService, (TransactionTemplate) null);
    }

    /**
     * 内部统一构造器。
     *
     * @param epayConfig 易支付配置
     * @param alipayConfig 支付宝配置
     * @param wxPayService 微信支付服务
     * @param orderMapper 订单 Mapper
     * @param paymentPostActionSupport 支付后置动作支撑服务
     * @param orderStateTransitionSupport 订单状态机
     * @param paymentFlowService 支付流水服务
     * @param orderStatusLogService 订单状态日志服务
     * @param transactionTemplate 事务模板，单元测试可为空
     */
    private PaymentCallbackSupport(EpayConfig epayConfig, AlipayConfig alipayConfig, WxPayService wxPayService,
                                   OrderMapper orderMapper, PaymentPostActionSupport paymentPostActionSupport,
                                   OrderStateTransitionSupport orderStateTransitionSupport,
                                   PaymentFlowService paymentFlowService,
                                   OrderStatusLogService orderStatusLogService,
                                   TransactionTemplate transactionTemplate) {
        this.epayConfig = epayConfig;
        this.alipayConfig = alipayConfig;
        this.wxPayService = wxPayService;
        this.orderMapper = orderMapper;
        this.paymentPostActionSupport = paymentPostActionSupport;
        this.orderStateTransitionSupport = orderStateTransitionSupport;
        this.paymentFlowService = paymentFlowService;
        this.orderStatusLogService = orderStatusLogService;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * 处理微信支付回调。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @return 微信回调响应报文
     */
    public String wxPayNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
            WxPayOrderNotifyResult result = wxPayService.parseOrderNotifyResult(xmlResult);
            log.info("处理微信支付回调, orderSn={}, transactionId={}", result.getOutTradeNo(), result.getTransactionId());
            PaymentProcessResult processResult = markOrderPaid(result.getOutTradeNo(),
                    result.getTransactionId(),
                    BaseWxPayResult.fenToYuan(result.getTotalFee()),
                    true,
                    PaymentNotifyChannelEnum.WECHAT);
            if (processResult.success()) {
                return WxPayNotifyResponse.success(processResult.message());
            }
            return WxPayNotifyResponse.fail(processResult.message());
        } catch (Exception e) {
            log.error("微信回调结果异常", e);
            return WxPayNotifyResponse.fail(e.getMessage());
        }
    }

    /**
     * 处理支付宝支付回调。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @return 支付宝回调响应文本
     */
    public String aliPayNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, String> paramsMap = buildStringParameterMap(request);
            boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(), alipayConfig.getSigntype());
            if (!signVerified) {
                log.error("支付宝支付回调：验签失败");
                return "error";
            }

            PaymentProcessResult processResult = markOrderPaid(request.getParameter("out_trade_no"),
                    request.getParameter("trade_no"),
                    request.getParameter("total_amount"),
                    true,
                    PaymentNotifyChannelEnum.ALIPAY);
            log.info("处理支付宝支付回调完成, orderSn={}, tradeNo={}, success={}",
                    request.getParameter("out_trade_no"),
                    request.getParameter("trade_no"),
                    processResult.success());
            return processResult.success() ? "success" : "error";
        } catch (Exception e) {
            log.error("支付宝支付回调异常", e);
            return "error";
        }
    }

    /**
     * 处理易支付回调。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @return 易支付回调响应文本
     */
    public String epayPayNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            String epaySign = request.getParameter("sign");
            Map<String, Object> paramsMap = buildObjectParameterMap(request);
            String sign = EpaySignUtil.sign(paramsMap, epayConfig.getKey());
            if (!StringUtils.equals(epaySign, sign)) {
                log.error("epayPayNotify epaySign not equals sign.");
                return "error";
            }

            PaymentProcessResult processResult = markOrderPaid(request.getParameter("out_trade_no"),
                    request.getParameter("trade_no"),
                    request.getParameter("money"),
                    true,
                    PaymentNotifyChannelEnum.EPAY);
            log.info("处理易支付回调完成, orderSn={}, tradeNo={}, success={}",
                    request.getParameter("out_trade_no"),
                    request.getParameter("trade_no"),
                    processResult.success());
            return processResult.success() ? "success" : "error";
        } catch (Exception e) {
            log.error("易支付回调异常", e);
            return "error";
        }
    }

    /**
     * 构建字符串参数 Map。
     * 支付宝 SDK 验签要求 Map 值为字符串，统一在这里取每个参数的第一个值，避免回调方法里重复拼装。
     *
     * @param request HTTP 请求
     * @return 字符串参数 Map
     */
    private Map<String, String> buildStringParameterMap(HttpServletRequest request) {
        Map<String, String> paramsMap = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> paramsMap.put(key, firstParameterValue(value)));
        return paramsMap;
    }

    /**
     * 构建对象参数 Map。
     * 易支付签名工具使用 Object 值类型，保留独立方法能让渠道差异集中在参数适配层。
     *
     * @param request HTTP 请求
     * @return 对象参数 Map
     */
    private Map<String, Object> buildObjectParameterMap(HttpServletRequest request) {
        Map<String, Object> paramsMap = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> paramsMap.put(key, firstParameterValue(value)));
        return paramsMap;
    }

    /**
     * 读取请求参数第一个值。
     *
     * @param values 参数值数组
     * @return 第一个参数值，空数组时返回空字符串
     */
    private String firstParameterValue(String[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        return values[0];
    }

    /**
     * 统一执行支付成功落单逻辑。
     *
     * @param orderSn 订单号
     * @param payId 第三方支付流水号
     * @param totalFee 支付金额
     * @param validateAmount 是否校验金额
     * @param channel 支付渠道
     * @return 支付处理结果
     */
    private PaymentProcessResult markOrderPaid(String orderSn, String payId, String totalFee,
                                               boolean validateAmount, PaymentNotifyChannelEnum channel) {
        if (StringUtils.isBlank(payId)) {
            log.error("{}：支付流水号为空，orderSn={}", channel.getDescription(), orderSn);
            return PaymentProcessResult.fail("支付流水号为空");
        }
        Order order = findOrderByOrderSn(orderSn);
        if (order == null) {
            log.error("{}：订单不存在，orderSn：{}", channel.getDescription(), orderSn);
            return PaymentProcessResult.fail("订单不存在");
        }
        if (validateAmount && isPaymentAmountMismatch(totalFee, order.getActualPrice())) {
            log.error("{}：支付金额不符合，orderSn：{}，totalFee：{}", channel.getDescription(), order.getOrderSn(), totalFee);
            return PaymentProcessResult.fail("支付金额不符合");
        }
        if (OrderUtil.hasPayed(order)) {
            log.info("{}：订单已经处理过了，orderSn：{}", channel.getDescription(), orderSn);
            return PaymentProcessResult.success("已处理");
        }
        if (!orderStateTransitionSupport.canTransition(order.getOrderStatus(), OrderStatusEnum.STATUS_PAY)) {
            log.error("{}：订单当前状态不可支付，orderSn={}，status={}",
                    channel.getDescription(), orderSn, order.getOrderStatus());
            return PaymentProcessResult.fail("订单当前状态不可支付");
        }

        return executePaidTransaction(order, payId, channel);
    }

    /**
     * 按订单号查询订单。
     *
     * @param orderSn 订单号
     * @return 订单，不存在时返回 null
     */
    private Order findOrderByOrderSn(String orderSn) {
        return orderMapper.selectOne(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderSn, orderSn));
    }

    /**
     * 判断渠道回调金额和订单应付金额是否不一致。
     * 这里保留 BigDecimal 的严格解析语义，非法金额会继续抛出异常并由渠道回调入口统一返回失败。
     *
     * @param totalFee 渠道回调金额
     * @param actualPrice 订单实付金额
     * @return true=金额不一致
     */
    private boolean isPaymentAmountMismatch(String totalFee, BigDecimal actualPrice) {
        return new BigDecimal(totalFee).compareTo(actualPrice) != 0;
    }

    /**
     * 执行支付成功事务。
     * 订单状态更新和支付后置本地消息写入必须同事务提交，避免订单已支付但后置副作用消息丢失。
     *
     * @param order 订单
     * @param payId 第三方支付流水号
     * @param channel 支付渠道
     * @return 支付处理结果
     */
    private PaymentProcessResult executePaidTransaction(Order order, String payId, PaymentNotifyChannelEnum channel) {
        if (transactionTemplate == null) {
            return doExecutePaidTransaction(order, payId, channel);
        }
        return transactionTemplate.execute(status -> doExecutePaidTransaction(order, payId, channel));
    }

    /**
     * 执行支付成功状态更新和后置消息写入。
     *
     * @param order 订单
     * @param payId 第三方支付流水号
     * @param channel 支付渠道
     * @return 支付处理结果
     */
    private PaymentProcessResult doExecutePaidTransaction(Order order, String payId, PaymentNotifyChannelEnum channel) {
        int updated = updateOrderToPaid(order, payId);
        if (updated == 0) {
            return handlePaidUpdateMiss(order, channel);
        }

        savePaymentFlowOrThrow(order, payId, channel);
        orderStatusLogService.recordSuccess(buildPaidStatusLogCommand(order, channel));
        paymentPostActionSupport.handleOrderPaid(order.getId());
        return PaymentProcessResult.success("处理成功");
    }

    /**
     * 将订单从待支付更新为已支付。
     * 通过状态机补充期望状态条件，避免支付回调、超时取消、重复通知并发时互相覆盖。
     *
     * @param order 订单
     * @param payId 第三方支付流水号
     * @return 影响行数
     */
    private int updateOrderToPaid(Order order, String payId) {
        var updateWrapper = Wrappers.lambdaUpdate(Order.class)
                .set(Order::getPayId, payId)
                .set(Order::getPayTime, LocalDateTime.now())
                .set(Order::getOrderStatus, OrderStatusEnum.STATUS_PAY.getStatus())
                .set(Order::getUpdateTime, new Date())
                .eq(Order::getId, order.getId());
        orderStateTransitionSupport.applyExpectedStatus(updateWrapper, OrderStatusEnum.STATUS_CREATE);
        return orderMapper.update(null, updateWrapper);
    }

    /**
     * 处理支付状态条件更新未命中的场景。
     * 未命中不一定是失败，可能是另一个并发回调已经把订单推进到已支付。
     *
     * @param order 原始订单
     * @param channel 支付渠道
     * @return 支付处理结果
     */
    private PaymentProcessResult handlePaidUpdateMiss(Order order, PaymentNotifyChannelEnum channel) {
        Order latestOrder = orderMapper.selectById(order.getId());
        if (latestOrder != null && OrderUtil.hasPayed(latestOrder)) {
            log.info("{}：订单已经被其他请求处理，orderSn：{}", channel.getDescription(), order.getOrderSn());
            return PaymentProcessResult.success("已处理");
        }
        log.error("{}：更新订单状态失败，orderSn：{}", channel.getDescription(), order.getOrderSn());
        return PaymentProcessResult.fail("更新订单状态失败");
    }

    /**
     * 保存内部支付流水。
     * 支付流水冲突说明同一渠道流水试图绑定不同订单，必须抛异常让事务回滚订单状态更新。
     *
     * @param order 订单
     * @param payId 第三方支付流水号
     * @param channel 支付渠道
     */
    private void savePaymentFlowOrThrow(Order order, String payId, PaymentNotifyChannelEnum channel) {
        PaymentFlowSaveResult saveResult = paymentFlowService.savePaidFlow(buildPaymentFlowCommand(order, payId, channel));
        if (saveResult == PaymentFlowSaveResult.DUPLICATE_CONFLICT) {
            throw new IllegalStateException("支付流水冲突");
        }
    }

    /**
     * 构建支付流水创建命令。
     *
     * @param order 订单
     * @param payId 第三方支付流水号
     * @param channel 支付渠道
     * @return 支付流水创建命令
     */
    private PaymentFlowCreateCommand buildPaymentFlowCommand(Order order, String payId,
                                                             PaymentNotifyChannelEnum channel) {
        return PaymentFlowCreateCommand.builder()
                .flowKey(channel.getCode() + ":" + payId)
                .orderId(order.getId())
                .orderSn(order.getOrderSn())
                .payId(payId)
                .payChannel(channel.getCode())
                .payAmount(order.getActualPrice())
                .build();
    }

    /**
     * 构建支付成功状态日志命令。
     *
     * @param order 订单
     * @param channel 支付渠道
     * @return 状态日志命令
     */
    private OrderStatusChangeCommand buildPaidStatusLogCommand(Order order, PaymentNotifyChannelEnum channel) {
        return OrderStatusChangeCommand.builder()
                .orderId(order.getId())
                .orderSn(order.getOrderSn())
                .sourceStatus(OrderStatusEnum.STATUS_CREATE)
                .targetStatus(OrderStatusEnum.STATUS_PAY)
                .changeType(OrderStatusChangeTypeEnum.PAY_CALLBACK)
                .operatorType("SYSTEM")
                .operatorId(channel.getCode())
                .remark(channel.getDescription() + "支付回调")
                .build();
    }

    /**
     * 支付处理结果。
     * 用于在不同支付渠道的回调中统一传递处理状态和响应信息。
     */
    private record PaymentProcessResult(boolean success, String message) {

        /**
         * 构建成功结果。
         *
         * @param message 响应信息
         * @return 成功结果
         */
        private static PaymentProcessResult success(String message) {
            return new PaymentProcessResult(true, message);
        }

        /**
         * 构建失败结果。
         *
         * @param message 响应信息
         * @return 失败结果
         */
        private static PaymentProcessResult fail(String message) {
            return new PaymentProcessResult(false, message);
        }
    }
}
