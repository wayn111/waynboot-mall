package com.wayn.domain.trade.support.order;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.domain.api.cart.entity.Cart;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.entity.OrderGoods;
import com.wayn.domain.api.promotion.entity.ShopMemberCoupon;
import com.wayn.domain.api.trade.mapper.OrderMapper;
import com.wayn.domain.api.cart.service.ICartService;
import com.wayn.domain.api.trade.service.IOrderGoodsService;
import com.wayn.domain.api.promotion.service.ShopMemberCouponService;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 下单持久化支撑服务。
 * 专注处理订单主表、订单商品、购物车清理和优惠券占用，供下单编排层复用，避免编排层混入数据库写入细节。
 */
@Service
@AllArgsConstructor
public class OrderSubmitPersistenceSupport {

    private final ICartService cartService;
    private final IOrderGoodsService orderGoodsService;
    private final ShopMemberCouponService shopMemberCouponService;
    private final OrderMapper orderMapper;
    private final OrderAssemblerSupport orderAssemblerSupport;

    /**
     * 判断订单号是否已经落库。
     *
     * @param orderSn 订单号
     * @return true=订单已存在；false=订单不存在
     */
    public boolean existsOrder(String orderSn) {
        return orderMapper.selectOne(Wrappers.lambdaQuery(Order.class)
                .select(Order::getId)
                .eq(Order::getOrderSn, orderSn)
                .last("limit 1")) != null;
    }

    /**
     * 持久化单笔订单。
     * 当前方法必须在下单事务内调用，订单主表、订单商品、购物车清理和优惠券占用共同提交或回滚。
     *
     * @param orderDTO 下单 DTO
     * @param context 下单上下文
     * @param order 待入库订单对象
     */
    public void persistSingle(OrderDTO orderDTO, OrderSubmitContext context, Order order) {
        if (orderMapper.insert(order) != 1) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
        }
        List<OrderGoods> orderGoodsList = orderAssemblerSupport.buildOrderGoods(order.getId(), context.checkedGoodsList());
        saveOrderGoods(orderGoodsList);
        clearSubmittedCart(orderDTO.getUserId(), orderDTO.getCartIdArr());
        markCouponUsed(orderDTO.getUserCouponId(), order.getId());
    }

    /**
     * 保存订单商品明细。
     *
     * @param orderGoodsList 待保存订单商品明细
     */
    private void saveOrderGoods(List<OrderGoods> orderGoodsList) {
        if (!orderGoodsService.saveBatch(orderGoodsList)) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR);
        }
    }

    /**
     * 清理已提交购物车。
     *
     * @param userId 用户 ID
     * @param cartIdArr 购物车 ID 列表
     */
    private void clearSubmittedCart(Long userId, List<Long> cartIdArr) {
        if (CollectionUtils.isEmpty(cartIdArr)) {
            cartService.remove(Wrappers.lambdaQuery(Cart.class)
                    .eq(Cart::getUserId, userId)
                    .eq(Cart::getChecked, true));
            return;
        }
        cartService.remove(Wrappers.lambdaQuery(Cart.class)
                .eq(Cart::getUserId, userId)
                .in(Cart::getId, cartIdArr));
    }

    /**
     * 占用用户优惠券。
     *
     * @param userCouponId 用户优惠券 ID
     * @param orderId 订单 ID
     */
    private void markCouponUsed(Long userCouponId, Long orderId) {
        if (userCouponId == null) {
            return;
        }
        // 通过 useStatus 条件更新占用优惠券，避免重复下单时把同一张券绑定多次。
        boolean updated = shopMemberCouponService.lambdaUpdate()
                .set(ShopMemberCoupon::getUseStatus, 1)
                .set(ShopMemberCoupon::getOrderId, orderId)
                .set(ShopMemberCoupon::getUpdateTime, new Date())
                .eq(ShopMemberCoupon::getId, userCouponId)
                .eq(ShopMemberCoupon::getUseStatus, 0)
                .update();
        if (!updated) {
            throw new BusinessException(ReturnCodeEnum.ORDER_SUBMIT_ERROR, "优惠卷不可用");
        }
    }
}
