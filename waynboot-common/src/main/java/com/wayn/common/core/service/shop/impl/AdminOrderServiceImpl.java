package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.config.WaynConfig;
import com.wayn.common.core.domain.shop.Member;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.shop.OrderGoods;
import com.wayn.common.core.domain.vo.ShipVO;
import com.wayn.common.core.mapper.shop.AdminOrderMapper;
import com.wayn.common.core.service.shop.*;
import com.wayn.common.core.util.OrderUtil;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class AdminOrderServiceImpl extends ServiceImpl<AdminOrderMapper, Order> implements IAdminOrderService {

    private AdminOrderMapper adminOrderMapper;
    private IOrderGoodsService iOrderGoodsService;
    private IGoodsProductService iGoodsProductService;
    private IMemberService iMemberService;
    private IMailService iMailService;

    @Override
    public IPage<Order> listPage(IPage<Order> page, Order order) {
        return adminOrderMapper.selectOrderListPage(page, order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R refund(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            return R.error();
        }

        //  如果订单不是退款状态，则不能退款
        if (!order.getOrderStatus().equals(OrderUtil.STATUS_REFUND)) {
            return R.error(ReturnCodeEnum.ORDER_CANNOT_REFUND_ERROR);
        }

        // 微信退款
//        WxPayRefundRequest wxPayRefundRequest = new WxPayRefundRequest();
//        wxPayRefundRequest.setOutTradeNo(order.getOrderSn());
//        wxPayRefundRequest.setOutRefundNo("refund_" + order.getOrderSn());
//        // 元转成分
//        Integer totalFee = order.getActualPrice().multiply(new BigDecimal(100)).intValue();
//        wxPayRefundRequest.setTotalFee(totalFee);
//        wxPayRefundRequest.setRefundFee(totalFee);
//
//        WxPayRefundResult wxPayRefundResult;
//        try {
//            wxPayRefundResult = wxPayService.refund(wxPayRefundRequest);
//        } catch (WxPayException e) {
//            log.error(e.getMessage(), e);
//            return R.error("订单退款失败");
//        }
//        if (!wxPayRefundResult.getReturnCode().equals("SUCCESS")) {
//            log.warn("refund fail: " + wxPayRefundResult.getReturnMsg());
//            return R.error("订单退款失败");
//        }
//        if (!wxPayRefundResult.getResultCode().equals("SUCCESS")) {
//            log.warn("refund fail: " + wxPayRefundResult.getReturnMsg());
//            return R.error("订单退款失败");
//        }

        LocalDateTime now = LocalDateTime.now();
        // 设置订单取消状态
        order.setOrderStatus(OrderUtil.STATUS_REFUND_CONFIRM);
        order.setOrderEndTime(now);
        // 记录订单退款相关信息
        order.setRefundAmount(order.getActualPrice());
        order.setRefundType("微信退款接口");
//        order.setRefundContent(wxPayRefundResult.getRefundId());
        order.setRefundContent("已退款");
        order.setRefundTime(now);
        order.setUpdateTime(new Date());
        updateById(order);
        // 商品货品数量增加
        List<OrderGoods> orderGoodsList = iOrderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_id", orderId));
        for (OrderGoods orderGoods : orderGoodsList) {
            Long productId = orderGoods.getProductId();
            Integer number = orderGoods.getNumber();
            if (!iGoodsProductService.addStock(productId, number)) {
                throw new RuntimeException("商品货品库存增加失败");
            }
        }

        // 返还优惠券
//        List<LitemallCouponUser> couponUsers = couponUserService.findByOid(orderId);
//        for (LitemallCouponUser couponUser: couponUsers) {
//            // 优惠券状态设置为可使用
//            couponUser.setStatus(CouponUserConstant.STATUS_USABLE);
//            couponUser.setUpdateTime(LocalDateTime.now());
//            couponUserService.update(couponUser);
//        }

        // 退款成功通知用户, 例如“您申请的订单退款 [ 单号:{1} ] 已成功，请耐心等待到账。”
        // 注意订单号只发后6位
        String email = iMemberService.getById(order.getUserId()).getEmail();
        if (StringUtils.isNotEmpty(email)) {
            iMailService.sendEmail("订单已经退款", order.getOrderSn().substring(8, 14), email,
                    WaynConfig.getAdminUrl() + "/message/email");
        }
        // logHelper.logOrderSucceed("退款", "订单编号 " + order.getOrderSn());
        return R.success();
    }

    @Override
    public R ship(ShipVO shipVO) {
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
                    + "/message/email");
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
