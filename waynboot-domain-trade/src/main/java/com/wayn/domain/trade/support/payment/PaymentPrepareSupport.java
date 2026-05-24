package com.wayn.domain.trade.support.payment;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.entity.OrderGoods;
import com.wayn.domain.api.trade.mapper.OrderMapper;
import com.wayn.domain.api.trade.service.IOrderGoodsService;
import com.wayn.domain.trade.support.order.OrderValidationSupport;
import com.wayn.common.design.strategy.pay.context.PayTypeContext;
import com.wayn.common.design.strategy.pay.strategy.PayTypeInterface;
import com.wayn.domain.api.trade.request.OrderPayReqVO;
import com.wayn.domain.api.trade.response.OrderPayResVO;
import com.wayn.util.enums.OrderStatusEnum;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.ServletUtils;
import com.wayn.util.util.ip.IpUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 支付准备支撑服务。
 * 负责支付前的订单校验、订单商品摘要组装，以及支付方式的原子更新。
 */
@Service
@AllArgsConstructor
public class PaymentPrepareSupport {

    private final OrderMapper orderMapper;
    private final IOrderGoodsService orderGoodsService;
    private final PayTypeContext payTypeContext;
    private final OrderValidationSupport orderValidationSupport;

    /**
     * 执行预支付准备。
     *
     * @param reqVO 支付请求
     * @return 预支付结果
     */
    public OrderPayResVO prepay(OrderPayReqVO reqVO) {
        Order order = orderMapper.selectOne(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderSn, reqVO.getOrderSn()));
        orderValidationSupport.ensurePayable(order);

        List<OrderGoods> orderGoodsList = orderGoodsService.list(Wrappers.lambdaQuery(OrderGoods.class)
                .eq(OrderGoods::getOrderId, order.getId()));
        reqVO.setActualPrice(order.getActualPrice());
        reqVO.setClientIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
        reqVO.setGoodsName(StringUtils.join(orderGoodsList.stream().map(OrderGoods::getGoodsName).toList(), ","));

        // 支付方式只允许在待支付状态下写入，避免已关闭或已支付订单被重复覆盖。
        int updated = orderMapper.update(null, Wrappers.lambdaUpdate(Order.class)
                .set(Order::getPayType, reqVO.getPayType())
                .eq(Order::getId, order.getId())
                .eq(Order::getOrderStatus, OrderStatusEnum.STATUS_CREATE.getStatus()));
        if (updated == 0) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SET_PAY_ERROR);
        }

        PayTypeInterface payType = payTypeContext.getInstance(reqVO.getPayType());
        return payType.pay(reqVO);
    }
}
