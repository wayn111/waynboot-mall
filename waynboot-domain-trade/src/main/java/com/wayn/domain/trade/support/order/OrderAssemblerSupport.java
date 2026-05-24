package com.wayn.domain.trade.support.order;

import com.wayn.domain.api.cart.entity.Cart;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.entity.OrderGoods;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.util.enums.OrderStatusEnum;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单组装支撑服务。
 * 负责把下单上下文转换为订单主表和订单商品表对象，减少编排服务中的对象拼装噪音。
 */
@Service
public class OrderAssemblerSupport {

    /**
     * 根据下单 DTO 和下单上下文组装订单主表对象。
     *
     * @param orderDTO 下单请求 DTO
     * @param context 下单上下文
     * @return 待入库订单对象
     */
    public Order buildOrder(OrderDTO orderDTO, OrderSubmitContext context) {
        Order order = new Order();
        order.setUserId(orderDTO.getUserId());
        order.setOrderSn(orderDTO.getOrderSn());
        order.setOrderStatus(OrderStatusEnum.STATUS_CREATE.getStatus());
        order.setConsignee(context.address().getName());
        order.setMobile(context.address().getTel());
        order.setMessage(orderDTO.getMessage() == null ? "" : orderDTO.getMessage());
        order.setAddress(context.address().getProvince() + context.address().getCity()
                + context.address().getCounty() + " " + context.address().getAddressDetail());
        order.setFreightPrice(context.freightPrice());
        order.setCouponPrice(context.couponPrice());
        order.setGoodsPrice(context.checkedGoodsPrice());
        order.setOrderPrice(context.orderTotalPrice());
        order.setActualPrice(context.actualPrice());
        order.setCreateTime(new Date());
        return order;
    }

    /**
     * 根据购物车快照批量组装订单商品对象。
     *
     * @param orderId 订单 ID
     * @param checkedGoodsList 已勾选购物车商品
     * @return 待入库订单商品列表
     */
    public List<OrderGoods> buildOrderGoods(Long orderId, List<Cart> checkedGoodsList) {
        List<OrderGoods> orderGoodsList = new ArrayList<>(checkedGoodsList.size());
        for (Cart cartGoods : checkedGoodsList) {
            OrderGoods orderGoods = new OrderGoods();
            orderGoods.setOrderId(orderId);
            orderGoods.setGoodsId(cartGoods.getGoodsId());
            orderGoods.setGoodsSn(cartGoods.getGoodsSn());
            orderGoods.setProductId(cartGoods.getProductId());
            orderGoods.setGoodsName(cartGoods.getGoodsName());
            orderGoods.setPicUrl(cartGoods.getPicUrl());
            orderGoods.setPrice(cartGoods.getPrice());
            orderGoods.setNumber(cartGoods.getNumber());
            orderGoods.setSpecifications(cartGoods.getSpecifications());
            orderGoods.setCreateTime(LocalDateTime.now());
            orderGoodsList.add(orderGoods);
        }
        return orderGoodsList;
    }
}
