package com.wayn.common.core.service.shop.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wayn.common.config.AlipayConfig;
import com.wayn.common.config.WaynConfig;
import com.wayn.common.core.domain.shop.Member;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.shop.OrderGoods;
import com.wayn.common.core.domain.vo.ShipVO;
import com.wayn.common.core.mapper.shop.AdminOrderMapper;
import com.wayn.common.core.service.shop.*;
import com.wayn.common.core.util.OrderUtil;
import com.wayn.common.enums.PayTypeEnum;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.OrderSnGenUtil;
import com.wayn.common.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class OrderServiceImpl extends ServiceImpl<AdminOrderMapper, Order> implements IOrderService {

    private AdminOrderMapper adminOrderMapper;
    private IOrderGoodsService iOrderGoodsService;
    private IGoodsProductService iGoodsProductService;
    private IMemberService iMemberService;
    private IMailService iMailService;
    private OrderSnGenUtil orderSnGenUtil;
    private AlipayConfig alipayConfig;
    private WxPayService wxPayService;

    @Override
    public IPage<Order> listPage(IPage<Order> page, Order order) {
        return adminOrderMapper.selectOrderListPage(page, order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R refund(Long orderId) throws UnsupportedEncodingException, WxPayException, AlipayApiException {
        Order order = getById(orderId);
        if (order == null) {
            return R.error();
        }

        //  如果订单不是退款状态，则不能退款
        if (!order.getOrderStatus().equals(OrderUtil.STATUS_REFUND)) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_REFUND_ERROR);
        }

        Integer payType = order.getPayType();
        String refundId = "";
        switch (Objects.requireNonNull(PayTypeEnum.of(payType))) {
            case WX -> {
                WxPayRefundV3Request refundV3Request = new WxPayRefundV3Request();
                String refundSn = orderSnGenUtil.generateRefundOrderSn();
                refundV3Request.setTransactionId(order.getPayId());
                refundV3Request.setOutRefundNo(refundSn);
                refundV3Request.setReason("商城退款：refund：{}" + refundSn);
                WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
                amount.setRefund(order.getActualPrice().multiply(new BigDecimal("100")).intValue());
                amount.setCurrency("CNY");
                amount.setTotal(order.getActualPrice().multiply(new BigDecimal("100")).intValue());
                refundV3Request.setAmount(amount);
                WxPayRefundV3Result refundV3Result = wxPayService.refundV3(refundV3Request);
                log.info("response:{}", JSON.toJSONString(refundV3Result));
                String status = refundV3Result.getStatus();
                if (!"SUCCESS".equals(status)) {
                    return R.error(ReturnCodeEnum.ORDER_REFUND_ERROR);
                }
                refundId = refundV3Result.getRefundId();
            }
            case ALI -> {
                AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig.getGateway(), alipayConfig.getAppId(),
                        alipayConfig.getRsaPrivateKey(), alipayConfig.getFormat(), alipayConfig.getCharset(), alipayConfig.getAlipayPublicKey(),
                        alipayConfig.getSigntype());
                AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
                JSONObject bizContent = new JSONObject();
                String refundSn = orderSnGenUtil.generateRefundOrderSn();
                bizContent.put("trade_no", order.getPayId());
                bizContent.put("refund_amount", order.getActualPrice());
                bizContent.put("out_request_no", refundSn);
                bizContent.put("refund_reason", "商城退款：refund：{}" + refundSn);

                request.setBizContent(bizContent.toString());
                AlipayTradeRefundResponse response = alipayClient.execute(request);
                log.info("response:{}", JSON.toJSONString(response));
                if (!response.isSuccess()) {
                    return R.error(ReturnCodeEnum.ORDER_REFUND_ERROR);
                }
                refundId = response.getTradeNo();
            }
            default -> {
            }
        }

        LocalDateTime now = LocalDateTime.now();
        // 设置订单取消状态
        order.setOrderStatus(OrderUtil.STATUS_REFUND_CONFIRM);
        order.setOrderEndTime(now);
        // 记录订单退款相关信息
        order.setRefundAmount(order.getActualPrice());
        order.setRefundType(payType);
        order.setRefundContent(refundId);
        order.setRefundTime(now);
        order.setUpdateTime(new Date());
        updateById(order);
        // 商品货品数量增加
        List<OrderGoods> orderGoodsList = iOrderGoodsService.list(new QueryWrapper<OrderGoods>()
                .eq("order_id", orderId));
        for (OrderGoods orderGoods : orderGoodsList) {
            Long productId = orderGoods.getProductId();
            Integer number = orderGoods.getNumber();
            if (!iGoodsProductService.addStock(productId, number)) {
                throw new RuntimeException("商品货品库存增加失败");
            }
        }

        // 退款成功通知用户, 例如“您申请的订单退款 [ 单号:{1} ] 已成功，请耐心等待到账。”
        // 注意订单号只发后6位
        String email = iMemberService.getById(order.getUserId()).getEmail();
        if (StringUtils.isNotEmpty(email)) {
            iMailService.sendEmail("订单已经退款", order.getOrderSn().substring(8, 14), email,
                    WaynConfig.getAdminUrl() + "/callback/email");
        }
        return R.success();
    }

    @Override
    public R ship(ShipVO shipVO) throws UnsupportedEncodingException {
        Long orderId = shipVO.getOrderId();
        String shipChannel = shipVO.getShipChannel();
        String shipSn = shipVO.getShipSn();
        Order order = getById(orderId);
        if (order == null || StringUtils.isEmpty(shipChannel) || StringUtils.isEmpty(shipSn)) {
            return R.error();
        }

        // 如果订单不是退款状态，则不能退款
        if (!order.getOrderStatus().equals(OrderUtil.STATUS_PAY)) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_SHIP_ERROR);
        }

        order.setOrderStatus(OrderUtil.STATUS_SHIP);
        order.setShipSn(shipSn);
        order.setShipChannel(shipChannel);
        order.setShipTime(LocalDateTime.now());
        order.setUpdateTime(new Date());
        updateById(order);

        // 发货会发送通知短信给用户:          *
        // "您的订单已经发货，快递公司 {1}，快递单 {2} ，请注意查收"
        String email = iMemberService.getById(order.getUserId()).getEmail();
        if (StringUtils.isNotEmpty(email)) {
            iMailService.sendEmail("您的订单已经发货，快递公司 申通，快递单 " + order.getOrderSn().substring(8, 14)
                    + "，请注意查收", order.getOrderSn().substring(8, 14), email, WaynConfig.getAdminUrl()
                    + "/callback/email");
        }
        return R.success();
    }

    @Override
    public R detail(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            return R.error();
        }
        List<OrderGoods> orderGoodsList = iOrderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_id", orderId));
        Member member = iMemberService.getById(order.getUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("orderGoods", orderGoodsList);
        data.put("user", member);
        return R.success().add("data", data);
    }
}
