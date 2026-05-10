package com.wayn.common.core.service.shop.support.payment;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.message.LocalMessage;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.service.message.LocalMessageCreateCommand;
import com.wayn.common.core.service.message.LocalMessageHandler;
import com.wayn.common.core.service.message.LocalMessageService;
import com.wayn.common.core.service.message.LocalMessageTopics;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.core.service.shop.support.order.OrderStockSupport;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付成功后的后置动作支撑服务。
 * 支付回调事务内只写本地消息，冻结库存确认和虚拟销量更新由本地消息 relay 重试执行，避免支付成功后置副作用丢失。
 */
@Slf4j
@Service
@AllArgsConstructor
public class PaymentPostActionSupport implements LocalMessageHandler {

    private static final String ORDER_PAID_POST_ACTION_KEY_PREFIX = "ORDER_PAID_POST_ACTION:";

    private final IOrderGoodsService orderGoodsService;
    private final IGoodsService goodsService;
    private final LocalMessageService localMessageService;
    private final OrderStockSupport orderStockSupport;

    /**
     * 处理支付成功后的后置动作。
     * 当前方法只保存本地消息，必须与订单支付状态更新处于同一业务事务，确保支付成功后置动作可重试。
     *
     * @param orderId 订单 ID
     */
    public void handleOrderPaid(Long orderId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", orderId);
        localMessageService.saveMessage(LocalMessageCreateCommand.builder()
                .messageKey(ORDER_PAID_POST_ACTION_KEY_PREFIX + orderId)
                .topic(LocalMessageTopics.ORDER_PAID_POST_ACTION)
                .bizType(LocalMessageTopics.BIZ_TYPE_ORDER)
                .bizId(String.valueOf(orderId))
                .payload(JSON.toJSONString(payload))
                .build());
    }

    /**
     * 判断是否支持支付成功后置动作消息。
     *
     * @param topic 消息主题
     * @return true=支持处理
     */
    @Override
    public boolean supports(String topic) {
        return LocalMessageTopics.ORDER_PAID_POST_ACTION.equals(topic);
    }

    /**
     * 处理支付成功后置动作本地消息。
     * 库存确认先执行，依赖库存流水幂等避免本地消息重试时重复扣减 locked_stock。
     *
     * @param message 本地消息
     */
    @Override
    public void handle(LocalMessage message) {
        Long orderId = JSON.parseObject(message.getPayload()).getLong("orderId");
        orderStockSupport.confirmFrozenStockByOrderId(orderId);
        updateVirtualSales(orderId);
    }

    /**
     * 更新订单商品对应的虚拟销量。
     *
     * @param orderId 订单 ID
     */
    private void updateVirtualSales(Long orderId) {
        try {
            List<OrderGoods> orderGoodsList = orderGoodsService.list(Wrappers.lambdaQuery(OrderGoods.class)
                    .eq(OrderGoods::getOrderId, orderId));
            for (OrderGoods orderGoods : orderGoodsList) {
                goodsService.updateVirtualSales(orderGoods.getGoodsId(), orderGoods.getNumber());
            }
        } catch (Exception e) {
            log.error("订单支付后置动作执行失败，orderId={}", orderId, e);
            throw new IllegalStateException("订单支付后置动作执行失败", e);
        }
    }
}
