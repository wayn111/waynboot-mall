package com.wayn.common.core.service.shop.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wayn.common.config.AlipayConfig;
import com.wayn.common.config.EpayConfig;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.service.shop.IMobileOrderService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.core.service.shop.IPayService;
import com.wayn.common.design.strategy.pay.context.PayTypeContext;
import com.wayn.common.design.strategy.pay.strategy.PayTypeInterface;
import com.wayn.common.request.OrderPayReqVO;
import com.wayn.common.response.OrderPayResVO;
import com.wayn.common.util.OrderHandleOption;
import com.wayn.common.util.OrderUtil;
import com.wayn.common.wapper.epay.util.EpaySignUtil;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.ServletUtils;
import com.wayn.util.util.ip.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单表 服务实现类
 *
 * @author wayn
 * @since 2020-08-11
 */
@Slf4j
@Service
@AllArgsConstructor
public class PayServiceImpl implements IPayService {

    private final EpayConfig epayConfig;
    private IMobileOrderService orderService;
    private IOrderGoodsService iOrderGoodsService;
    private PayTypeContext payTypeContext;
    private AlipayConfig alipayConfig;
    private WxPayService wxPayService;

    @Override
    public OrderPayResVO prepay(OrderPayReqVO reqVO) {
        String orderSn = reqVO.getOrderSn();
        Integer payType = reqVO.getPayType();
        // 获取订单详情
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
        // 获取订单商品详情
        Long orderId = order.getId();
        List<OrderGoods> orderGoods = iOrderGoodsService.list(Wrappers.lambdaQuery(OrderGoods.class).eq(OrderGoods::getOrderId, orderId));
        String goodsName = StringUtils.join(orderGoods.stream().map(OrderGoods::getGoodsName).toList(), ",");
        reqVO.setActualPrice(order.getActualPrice());
        reqVO.setClientIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
        reqVO.setGoodsName(goodsName);
        ReturnCodeEnum returnCodeEnum = orderService.checkOrderOperator(order);
        if (!ReturnCodeEnum.SUCCESS.equals(returnCodeEnum)) {
            throw new BusinessException(returnCodeEnum);
        }
        // 检测是否能够支付
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isPay()) {
            throw new BusinessException(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
        }
        // 保存支付方式
        boolean update = orderService.lambdaUpdate().set(Order::getPayType, payType).eq(Order::getOrderSn, orderSn).update();
        if (!update) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SET_PAY_ERROR);
        }
        PayTypeInterface instance = payTypeContext.getInstance(reqVO.getPayType());
        return instance.pay(reqVO);
    }

    @Override
    public String wxPayNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
            WxPayOrderNotifyResult result = wxPayService.parseOrderNotifyResult(xmlResult);
            log.info("处理腾讯支付平台的订单支付, xmlResult is {}", xmlResult);
            // 加入自己处理订单的业务逻辑，需要判断订单是否已经支付过，否则可能会重复调用
            String orderNo = result.getOutTradeNo();
            String totalFee = BaseWxPayResult.fenToYuan(result.getTotalFee());
            String payId = result.getTransactionId();

            Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_sn", orderNo));
            if (order == null) {
                log.error("微信支付回调：订单不存在，orderSn：{}", orderNo);
                return WxPayNotifyResponse.fail("订单不存在");
            }

            // 检查这个订单是否已经处理过
            if (OrderUtil.hasPayed(order)) {
                log.error("微信支付回调：订单已经处理过了，orderSn：{}", orderNo);
                return WxPayNotifyResponse.fail("订单已经处理过了");
            }

            // 检查支付订单金额
            if (!totalFee.equals(order.getActualPrice().toString())) {
                log.error("微信支付回调: 支付金额不符合，orderSn：{}，totalFee：{}", order.getOrderSn(), totalFee);
                return WxPayNotifyResponse.fail("支付金额不符合");
            }

            order.setPayId(payId);
            order.setPayTime(LocalDateTime.now());
            order.setOrderStatus(OrderStatusEnum.STATUS_PAY.getStatus());
            order.setUpdateTime(new Date());
            if (!orderService.updateById(order)) {
                log.error("微信支付回调: 更新订单状态失败，order：{}", JSON.toJSONString(order.getOrderSn()));
                return WxPayNotifyResponse.fail("更新订单状态失败");
            }

            return WxPayNotifyResponse.success("处理成功!");
        } catch (Exception e) {
            log.error("微信回调结果异常,异常原因{}", e.getMessage());
            return WxPayNotifyResponse.fail(e.getMessage());
        }
    }

    @Override
    public String aliPayNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, String[]> parameterMap = request.getParameterMap();
            Map<String, String> paramsMap = new HashMap<>();
            parameterMap.forEach((s, strings) -> paramsMap.put(s, strings[0]));
            // 调用SDK验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, alipayConfig.getAlipayPublicKey(), alipayConfig.getCharset(), alipayConfig.getSigntype());
            if (!signVerified) {
                log.error("支付宝支付回调：验签失败");
                return "error";
            }
            // 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            String orderSn = request.getParameter("out_trade_no");
            String tradeNo = request.getParameter("trade_no");
            Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
            if (order == null) {
                log.error("支付宝支付回调：订单不存在，orderSn：{}", orderSn);
                return "error";
            }

            // 检查这个订单是否已经处理过
            if (OrderUtil.hasPayed(order)) {
                log.error("支付宝支付回调：订单已经处理过了，orderSn：{}", orderSn);
                return "error";
            }

            order.setPayId(tradeNo);
            order.setPayTime(LocalDateTime.now());
            order.setOrderStatus(OrderStatusEnum.STATUS_PAY.getStatus());
            order.setUpdateTime(new Date());
            if (!orderService.updateById(order)) {
                log.error("支付宝支付回调: 更新订单状态失败，order：{}", JSON.toJSONString(order.getOrderSn()));
                return "error";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "error";
        }
        log.info("支付宝支付回调：结束");
        return "success";
    }

    @Override
    public String epayPayNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            String epaySign = request.getParameter("sign");
            Map<String, String[]> parameterMap = request.getParameterMap();
            Map<String, Object> paramsMap = new HashMap<>();
            parameterMap.forEach((s, strings) -> paramsMap.put(s, strings[0]));
            // 调用SDK验证签名
            String sign = EpaySignUtil.sign(paramsMap, epayConfig.getKey());
            if (!epaySign.equals(sign)) {
                log.error("epayPayNotify epaySign not equals sign.");
                return "error";
            }
            // 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            String orderSn = request.getParameter("out_trade_no");
            String tradeNo = request.getParameter("trade_no");
            Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
            if (order == null) {
                log.error("易支付回调：订单不存在，orderSn：{}", orderSn);
                return "error";
            }

            // 检查这个订单是否已经处理过
            if (OrderUtil.hasPayed(order)) {
                log.error("易支付回调：订单已经处理过了，orderSn：{}", orderSn);
                return "error";
            }
            order.setPayId(tradeNo);
            order.setPayTime(LocalDateTime.now());
            order.setOrderStatus(OrderStatusEnum.STATUS_PAY.getStatus());
            order.setUpdateTime(new Date());
            if (!orderService.updateById(order)) {
                log.error("易支付回调: 更新订单状态失败，order：{}", JSON.toJSONString(order.getOrderSn()));
                return "error";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "error";
        }
        log.info("易支付回调：结束");
        return "success";
    }
}
