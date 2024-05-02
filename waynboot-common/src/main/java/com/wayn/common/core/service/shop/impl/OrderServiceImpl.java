package com.wayn.common.core.service.shop.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.wayn.common.core.entity.shop.Member;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.mapper.shop.AdminOrderMapper;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.core.service.shop.IOrderService;
import com.wayn.common.core.vo.MemberVO;
import com.wayn.common.core.vo.OrderGoodsVO;
import com.wayn.common.core.vo.OrderVO;
import com.wayn.common.core.vo.ShipVO;
import com.wayn.common.design.strategy.pay.PayTypeEnum;
import com.wayn.common.design.strategy.refund.context.RefundContext;
import com.wayn.common.design.strategy.refund.strategy.RefundInterface;
import com.wayn.common.request.OrderManagerReqVO;
import com.wayn.common.request.OrderRefundReqVO;
import com.wayn.common.response.OrderDetailResVO;
import com.wayn.common.response.OrderManagerResVO;
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

@Slf4j
@Service
@AllArgsConstructor
public class OrderServiceImpl extends ServiceImpl<AdminOrderMapper, Order> implements IOrderService {

    private AdminOrderMapper adminOrderMapper;
    private IOrderGoodsService iOrderGoodsService;
    private IGoodsProductService iGoodsProductService;
    private IMemberService iMemberService;
    private RefundContext refundContext;
    private PlatformTransactionManager platformTransactionManager;

    @Override
    public IPage<OrderManagerResVO> listPage(IPage<Order> page, OrderManagerReqVO order) {
        IPage<OrderManagerResVO> orderManagerResVOIPage = adminOrderMapper.selectOrderListPage(page, order);
        for (OrderManagerResVO item : orderManagerResVOIPage.getRecords()) {
            item.setOrderStatusMsg(OrderStatusEnum.getDescByOrderStatus(item.getOrderStatus()));
            item.setRefundStatusMsg(RefundStatusEnum.getDescByRefundStatus(item.getRefundStatus()));
            item.setRefundTypeMsg(PayTypeEnum.getDescByPayType(item.getRefundType()));
            item.setPayTypeMsg(PayTypeEnum.getDescByPayType(item.getPayType()));
        }
        return orderManagerResVOIPage;
    }

    @Override
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
        // 商品货品数量增加
        List<OrderGoods> orderGoodsList = iOrderGoodsService.list(new QueryWrapper<OrderGoods>()
                .eq("order_id", order.getId()));
        // 如果订单不是申请退款状态，则不能退款
        if (!order.getOrderStatus().equals(OrderStatusEnum.STATUS_REFUND.getStatus())) {
            throw new BusinessException(ReturnCodeEnum.ORDER_CANNOT_REFUND_ERROR);
        }
        TransactionStatus transaction = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // 调用三方接口进行退款
            int refundStatus = 2;
            Short orderStatus = OrderStatusEnum.STATUS_REFUND.getStatus();
            try {
                RefundInterface instance = refundContext.getInstance(order.getPayType());
                reqVO.setPayId(order.getPayId());
                instance.refund(reqVO);
                orderStatus = OrderStatusEnum.STATUS_REFUND_CONFIRM.getStatus();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                refundStatus = 3;
                refundMoney = BigDecimal.ZERO;
                reqVO.setRefundReason(reqVO.getRefundReason() + " 退款失败：" + StringUtils.substring(e.getMessage(), 0, 2000));
            }
            Integer payType = order.getPayType();
            LocalDateTime now = LocalDateTime.now();
            // 设置订单取消状态
            order.setOrderStatus(orderStatus);
            order.setOrderEndTime(now);
            // 记录订单退款相关信息
            order.setRefundStatus(refundStatus);
            order.setRefundAmount(refundMoney);
            order.setRefundType(payType);
            order.setRefundContent(reqVO.getRefundReason());
            order.setRefundTime(now);
            order.setUpdateTime(new Date());
            updateById(order);
            if (refundStatus == 2) {
                for (OrderGoods orderGoods : orderGoodsList) {
                    Long productId = orderGoods.getProductId();
                    Integer number = orderGoods.getNumber();
                    if (!iGoodsProductService.addStock(productId, number)) {
                        throw new RuntimeException("商品货品库存增加失败");
                    }
                }
            }
            platformTransactionManager.commit(transaction);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            platformTransactionManager.rollback(transaction);
        }
    }

    private Order getByOrderSn(String orderSn) {
        return this.lambdaQuery().eq(Order::getOrderSn, orderSn).one();
    }

    @Override
    public void ship(ShipVO shipVO) {
        Long orderId = shipVO.getOrderId();
        String shipChannel = shipVO.getShipChannel();
        String shipSn = shipVO.getShipSn();
        Order order = getById(orderId);
        if (order == null || StringUtils.isEmpty(shipChannel) || StringUtils.isEmpty(shipSn)) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }

        // 如果订单不是支付状态，则不能发货
        if (!order.getOrderStatus().equals(OrderStatusEnum.STATUS_PAY.getStatus())) {
            throw new BusinessException(ReturnCodeEnum.ORDER_CANNOT_SHIP_ERROR);
        }

        order.setOrderStatus(OrderStatusEnum.STATUS_SHIP.getStatus());
        order.setShipSn(shipSn);
        order.setShipChannel(shipChannel);
        order.setShipTime(LocalDateTime.now());
        order.setUpdateTime(new Date());
        updateById(order);
    }

    @Override
    public OrderDetailResVO detail(Long orderId) {
        OrderDetailResVO orderDetailResVO = new OrderDetailResVO();
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ReturnCodeEnum.ERROR);
        }
        List<OrderGoods> orderGoodsList = iOrderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_id", orderId));
        Member member = iMemberService.getById(order.getUserId());
        OrderVO orderVO = BeanUtil.copyProperties(order, OrderVO.class);
        orderVO.setOrderStatusMsg(OrderStatusEnum.getDescByOrderStatus(orderVO.getOrderStatus()));
        orderVO.setRefundStatusMsg(RefundStatusEnum.getDescByRefundStatus(orderVO.getRefundStatus()));
        orderVO.setRefundTypeMsg(PayTypeEnum.getDescByPayType(orderVO.getRefundType()));
        orderVO.setPayTypeMsg(PayTypeEnum.getDescByPayType(orderVO.getPayType()));
        orderDetailResVO.setOrder(orderVO);
        orderDetailResVO.setOrderGoods(BeanUtil.copyToList(orderGoodsList, OrderGoodsVO.class));
        orderDetailResVO.setUser(BeanUtil.copyProperties(member, MemberVO.class));
        return orderDetailResVO;
    }

}
