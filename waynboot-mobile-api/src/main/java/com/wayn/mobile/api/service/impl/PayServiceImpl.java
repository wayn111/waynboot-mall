package com.wayn.mobile.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wayn.common.config.AlipayConfig;
import com.wayn.common.config.WaynConfig;
import com.wayn.common.core.domain.shop.Member;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.vo.OrderVO;
import com.wayn.common.core.service.shop.IMailService;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.common.core.util.OrderHandleOption;
import com.wayn.common.core.util.OrderUtil;
import com.wayn.common.enums.PayTypeEnum;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import com.wayn.common.util.ServletUtils;
import com.wayn.common.util.ip.IpUtils;
import com.wayn.mobile.api.service.IMobileOrderService;
import com.wayn.mobile.api.service.IPayService;
import com.wayn.common.util.OrderSnGenUtil;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    private IMemberService iMemberService;
    private IMailService iMailService;
    private AlipayConfig alipayConfig;
    private IMobileOrderService orderService;
    private WxPayService wxPayService;
    private OrderSnGenUtil orderSnGenUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R prepay(String orderSn, Integer payType, HttpServletRequest request) {
        // 获取订单详情
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
        ReturnCodeEnum returnCodeEnum = orderService.checkOrderOperator(order);
        if (!returnCodeEnum.equals(ReturnCodeEnum.SUCCESS)) {
            return R.error(returnCodeEnum);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isPay()) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
        }
        // 设置支付方式
        order.setPayType(payType);
        Member member = iMemberService.getById(MobileSecurityUtils.getUserId());
        String openid = member.getWeixinOpenid();
        if (openid == null) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
        }
        WxPayMpOrderResult result;
        try {
            WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
            orderRequest.setOutTradeNo(order.getOrderSn());
            orderRequest.setOpenid(openid);
            orderRequest.setBody("商城订单：" + order.getOrderSn());
            String url = WaynConfig.getMobileUrl();
            orderRequest.setNotifyUrl(url + "/pay/callback/weixinPayNotify");
            // 元转成分
            int fee;
            BigDecimal actualPrice = order.getActualPrice();
            fee = actualPrice.multiply(new BigDecimal(100)).intValue();
            orderRequest.setTotalFee(fee);
            orderRequest.setSpbillCreateIp(IpUtils.getIpAddr(request));
            result = wxPayService.createOrder(orderRequest);
            return R.success().add("result", result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public R h5pay(OrderVO orderVO) throws UnsupportedEncodingException {
        String orderSn = orderVO.getOrderSn();
        Integer payType = orderVO.getPayType();
        // 获取订单详情
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
        Long userId = order.getUserId();
        ReturnCodeEnum returnCodeEnum = orderService.checkOrderOperator(order);
        if (!ReturnCodeEnum.SUCCESS.equals(returnCodeEnum)) {
            return R.error(returnCodeEnum);
        }
        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isPay()) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
        }
        // 保存支付方式
        boolean update = orderService.lambdaUpdate()
                .set(Order::getPayType, payType)
                .eq(Order::getOrderSn, orderSn).update();
        if (!update) {
            return R.error(ReturnCodeEnum.ORDER_SET_PAY_ERROR);
        }
        switch (Objects.requireNonNull(PayTypeEnum.of(payType))) {
            case WX -> {
                WxPayMpOrderResult result;
                try {
                    WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
                    String url = WaynConfig.getMobileUrl();
                    orderRequest.setOutTradeNo(order.getOrderSn());
                    orderRequest.setTradeType(WxPayConstants.TradeType.MWEB);
                    orderRequest.setNotifyUrl(url + "/pay/callback/weixinPayNotify");
                    orderRequest.setBody("商城订单：" + order.getOrderSn());
                    // 元转成分
                    int fee;
                    BigDecimal actualPrice = order.getActualPrice();
                    fee = actualPrice.multiply(new BigDecimal(100)).intValue();
                    orderRequest.setTotalFee(fee);
                    orderRequest.setSpbillCreateIp(IpUtils.getIpAddr(ServletUtils.getRequest()));

                    result = wxPayService.createOrder(orderRequest);
                    return R.success().add("result", result);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return R.error(ReturnCodeEnum.ORDER_CANNOT_PAY_ERROR);
                }
            }
            case ALI -> {
                // 初始化
                AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig.getGateway(), alipayConfig.getAppId(),
                        alipayConfig.getRsaPrivateKey(), alipayConfig.getFormat(), alipayConfig.getCharset(), alipayConfig.getAlipayPublicKey(),
                        alipayConfig.getSigntype());
                // 创建API对应的request，使用手机网站支付request
                AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
                // 在公共参数中设置回跳和通知地址
                String url = WaynConfig.getMobileUrl();
                if (StringUtils.isNotBlank(orderVO.getReturnUrl())) {
                    alipayRequest.setReturnUrl(orderVO.getReturnUrl());
                }
                alipayRequest.setNotifyUrl(url + "/pay/callback/aliPayNotify");

                // 填充业务参数
                // 必填
                // 商户订单号，需保证在商户端不重复
                String out_trade_no = orderSn;
                // 销售产品码，与支付宝签约的产品码名称。目前仅支持FAST_INSTANT_TRADE_PAY
                String product_code = "FAST_INSTANT_TRADE_PAY";
                // 订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]。
                BigDecimal actualPrice = order.getActualPrice();
                String total_amount = actualPrice.toString();
                // 订单标题
                String subject = "商城订单";

                /******必传参数******/
                JSONObject bizContent = new JSONObject();
                // 商户订单号，商家自定义，保持唯一性
                bizContent.put("out_trade_no", out_trade_no);
                // 支付金额，最小值0.01元
                bizContent.put("total_amount", total_amount);
                // 订单标题，不可使用特殊符号
                bizContent.put("subject", subject);
                // 电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
                bizContent.put("product_code", product_code);

                alipayRequest.setBizContent(bizContent.toString());
                // 请求
                String form;
                try {
                    log.info("alipayRequest:{}", JSON.toJSONString(alipayRequest));
                    // 需要自行申请支付宝的沙箱账号、申请appID，并在配置文件中依次配置AppID、密钥、公钥，否则这里会报错。
                    form = alipayClient.pageExecute(alipayRequest).getBody();// 调用SDK生成表单
                    return R.success().add("form", form);
                } catch (AlipayApiException e) {
                    log.error(e.getMessage(), e);
                    return R.error(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
                }
            }
            case ALI_TEST -> {
                // 支付宝test，直接更新支付状态为已支付
                order.setPayId("xxxxx0987654321-ali");
                order.setPayTime(LocalDateTime.now());
                order.setOrderStatus(OrderUtil.STATUS_PAY);
                order.setUpdateTime(new Date());
                if (!orderService.updateById(order)) {
                    return R.error(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
                }
                // 订单支付成功以后，会发送短信给用户，以及发送邮件给管理员
                String email = iMemberService.getById(order.getUserId()).getEmail();
                if (StringUtils.isNotBlank(email)) {
                    iMailService.sendEmail("新订单通知", order.toString(), email, WaynConfig.getMobileUrl() + "/callback/email");
                }
                return R.success();
            }
            default -> {
                return R.error(ReturnCodeEnum.ORDER_NOT_SUPPORT_PAYWAY_ERROR);
            }
        }
    }

    @Override
    public R refund(Long orderId) throws UnsupportedEncodingException, AlipayApiException, WxPayException {
        Order order = orderService.getById(orderId);
        ReturnCodeEnum returnCodeEnum = orderService.checkOrderOperator(order);
        if (!ReturnCodeEnum.SUCCESS.equals(returnCodeEnum)) {
            return R.error(returnCodeEnum);
        }

        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isRefund()) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_REFUND_ERROR);
        }

        // 设置订单申请退款状态
        order.setOrderStatus(OrderUtil.STATUS_REFUND);
        order.setUpdateTime(new Date());
        if (!orderService.updateById(order)) {
            return R.error(ReturnCodeEnum.ERROR);
        }
        return R.success();
    }


    @Override
    public String wxPayNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
            WxPayOrderNotifyResult result = wxPayService.parseOrderNotifyResult(xmlResult);
            log.info("处理腾讯支付平台的订单支付, {}", result.getReturnMsg());
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
            order.setOrderStatus(OrderUtil.STATUS_PAY);
            order.setUpdateTime(new Date());
            if (!orderService.updateById(order)) {
                log.error("微信支付回调: 更新订单状态失败，order：{}", JSON.toJSONString(order.getOrderSn()));
                return WxPayNotifyResponse.fail("更新订单状态失败");
            }

            // 订单支付成功以后，会发送短信给用户，以及发送邮件给管理员
            String email = iMemberService.getById(order.getUserId()).getEmail();
            if (StringUtils.isNotBlank(email)) {
                iMailService.sendEmail("新订单通知", order.toString(), email, WaynConfig.getMobileUrl() + "/callback/email");
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
            order.setOrderStatus(OrderUtil.STATUS_PAY);
            order.setUpdateTime(new Date());
            if (!orderService.updateById(order)) {
                log.error("支付宝支付回调: 更新订单状态失败，order：{}", JSON.toJSONString(order.getOrderSn()));
                return "error";
            }
            // 订单支付成功以后，会发送短信给用户，以及发送邮件给管理员
            String email = iMemberService.getById(order.getUserId()).getEmail();
            if (StringUtils.isNotBlank(email)) {
                iMailService.sendEmail("新订单通知", order.toString(), email, WaynConfig.getMobileUrl() + "/callback/email");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "error";
        }
        log.info("支付宝支付回调：结束");
        return "success";
    }
}
