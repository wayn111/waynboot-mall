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
import com.wayn.common.core.mapper.shop.OrderMapper;
import com.wayn.common.core.service.shop.support.order.OrderStateTransitionSupport;
import com.wayn.common.util.OrderUtil;
import com.wayn.common.wapper.epay.util.EpaySignUtil;
import com.wayn.util.enums.OrderStatusEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
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
     * @param platformTransactionManager 事务管理器
     */
    @Autowired
    public PaymentCallbackSupport(EpayConfig epayConfig, AlipayConfig alipayConfig, WxPayService wxPayService,
                                  OrderMapper orderMapper, PaymentPostActionSupport paymentPostActionSupport,
                                  OrderStateTransitionSupport orderStateTransitionSupport,
                                  PlatformTransactionManager platformTransactionManager) {
        this(epayConfig, alipayConfig, wxPayService, orderMapper, paymentPostActionSupport,
                orderStateTransitionSupport, new TransactionTemplate(platformTransactionManager));
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
     */
    public PaymentCallbackSupport(EpayConfig epayConfig, AlipayConfig alipayConfig, WxPayService wxPayService,
                                  OrderMapper orderMapper, PaymentPostActionSupport paymentPostActionSupport,
                                  OrderStateTransitionSupport orderStateTransitionSupport) {
        this(epayConfig, alipayConfig, wxPayService, orderMapper, paymentPostActionSupport,
                orderStateTransitionSupport, (TransactionTemplate) null);
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
     * @param transactionTemplate 事务模板，单元测试可为空
     */
    private PaymentCallbackSupport(EpayConfig epayConfig, AlipayConfig alipayConfig, WxPayService wxPayService,
                                   OrderMapper orderMapper, PaymentPostActionSupport paymentPostActionSupport,
                                   OrderStateTransitionSupport orderStateTransitionSupport,
                                   TransactionTemplate transactionTemplate) {
        this.epayConfig = epayConfig;
        this.alipayConfig = alipayConfig;
        this.wxPayService = wxPayService;
        this.orderMapper = orderMapper;
        this.paymentPostActionSupport = paymentPostActionSupport;
        this.orderStateTransitionSupport = orderStateTransitionSupport;
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
                    "微信支付回调");
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
            Map<String, String[]> parameterMap = request.getParameterMap();
            Map<String, String> paramsMap = new HashMap<>();
            parameterMap.forEach((key, value) -> paramsMap.put(key, value[0]));
            boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(), alipayConfig.getSigntype());
            if (!signVerified) {
                log.error("支付宝支付回调：验签失败");
                return "error";
            }

            PaymentProcessResult processResult = markOrderPaid(request.getParameter("out_trade_no"),
                    request.getParameter("trade_no"),
                    null,
                    false,
                    "支付宝支付回调");
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
            Map<String, String[]> parameterMap = request.getParameterMap();
            Map<String, Object> paramsMap = new HashMap<>();
            parameterMap.forEach((key, value) -> paramsMap.put(key, value[0]));
            String sign = EpaySignUtil.sign(paramsMap, epayConfig.getKey());
            if (!epaySign.equals(sign)) {
                log.error("epayPayNotify epaySign not equals sign.");
                return "error";
            }

            PaymentProcessResult processResult = markOrderPaid(request.getParameter("out_trade_no"),
                    request.getParameter("trade_no"),
                    null,
                    false,
                    "易支付回调");
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
     * 统一执行支付成功落单逻辑。
     *
     * @param orderSn 订单号
     * @param payId 第三方支付流水号
     * @param totalFee 支付金额
     * @param validateAmount 是否校验金额
     * @param channel 渠道名称
     * @return 支付处理结果
     */
    private PaymentProcessResult markOrderPaid(String orderSn, String payId, String totalFee,
                                               boolean validateAmount, String channel) {
        Order order = orderMapper.selectOne(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderSn, orderSn));
        if (order == null) {
            log.error("{}：订单不存在，orderSn：{}", channel, orderSn);
            return PaymentProcessResult.fail("订单不存在");
        }
        if (validateAmount && new BigDecimal(totalFee).compareTo(order.getActualPrice()) != 0) {
            log.error("{}：支付金额不符合，orderSn：{}，totalFee：{}", channel, order.getOrderSn(), totalFee);
            return PaymentProcessResult.fail("支付金额不符合");
        }
        if (OrderUtil.hasPayed(order)) {
            log.info("{}：订单已经处理过了，orderSn：{}", channel, orderSn);
            return PaymentProcessResult.success("已处理");
        }
        if (!orderStateTransitionSupport.canTransition(order.getOrderStatus(), OrderStatusEnum.STATUS_PAY)) {
            log.error("{}：订单当前状态不可支付，orderSn={}，status={}", channel, orderSn, order.getOrderStatus());
            return PaymentProcessResult.fail("订单当前状态不可支付");
        }

        return executePaidTransaction(order, payId, channel);
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
    private PaymentProcessResult executePaidTransaction(Order order, String payId, String channel) {
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
    private PaymentProcessResult doExecutePaidTransaction(Order order, String payId, String channel) {
        // 通过“待支付 -> 已支付”的条件更新实现幂等，避免多个回调并发重复执行成功逻辑。
        int updated = orderMapper.update(null, Wrappers.lambdaUpdate(Order.class)
                .set(Order::getPayId, payId)
                .set(Order::getPayTime, LocalDateTime.now())
                .set(Order::getOrderStatus, OrderStatusEnum.STATUS_PAY.getStatus())
                .set(Order::getUpdateTime, new Date())
                .eq(Order::getId, order.getId())
                .eq(Order::getOrderStatus, OrderStatusEnum.STATUS_CREATE.getStatus()));
        if (updated == 0) {
            // 条件更新失败后再次查询最新状态，用于区分“被其他线程成功处理”与“真实更新失败”。
            Order latestOrder = orderMapper.selectById(order.getId());
            if (latestOrder != null && OrderUtil.hasPayed(latestOrder)) {
                log.info("{}：订单已经被其他请求处理，orderSn：{}", channel, order.getOrderSn());
                return PaymentProcessResult.success("已处理");
            }
            log.error("{}：更新订单状态失败，orderSn：{}", channel, order.getOrderSn());
            return PaymentProcessResult.fail("更新订单状态失败");
        }

        paymentPostActionSupport.handleOrderPaid(order.getId());
        return PaymentProcessResult.success("处理成功");
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
