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

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 支付成功后的后置动作支撑服务。
 * 支付回调事务内只写本地消息，冻结库存确认和虚拟销量更新由本地消息 relay 重试执行，避免支付成功后置副作用丢失。
 */
@Slf4j
@Service
@AllArgsConstructor
public class PaymentPostActionSupport implements LocalMessageHandler {

    private static final String ORDER_PAID_POST_ACTION_KEY_PREFIX = "ORDER_PAID_POST_ACTION:";
    private static final String ORDER_ID_PAYLOAD_FIELD = "orderId";

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
        validateOrderId(orderId);
        OrderPaidPostActionPayload payload = new OrderPaidPostActionPayload(orderId);
        localMessageService.saveMessage(LocalMessageCreateCommand.builder()
                .messageKey(ORDER_PAID_POST_ACTION_KEY_PREFIX + orderId)
                .topic(LocalMessageTopics.ORDER_PAID_POST_ACTION)
                .bizType(LocalMessageTopics.BIZ_TYPE_ORDER)
                .bizId(String.valueOf(orderId))
                .payload(payload.toJson())
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
        Long orderId = OrderPaidPostActionPayload.fromJson(message.getPayload()).orderId();
        validateOrderId(orderId);
        orderStockSupport.confirmFrozenStockByOrderId(orderId);
        updateVirtualSales(orderId);
    }

    /**
     * 校验支付后置动作消息中的订单 ID。
     * 本地消息 payload 异常时直接抛错，由 relay 记录失败并进入重试/补偿链路，避免吞掉非法消息。
     *
     * @param orderId 订单 ID
     */
    private void validateOrderId(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("支付后置动作缺少订单 ID");
        }
    }

    /**
     * 更新订单关联商品的虚拟销量。
     * 虚拟销量只是展示型数据，失败时记录日志并等待后续治理，不影响库存确认的幂等结果。
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
            log.error("虚拟销量更新失败，orderId={}，不影响库存确认结果", orderId, e);
        }
    }

    /**
     * 支付成功后置动作 payload。
     * 用内部 record 固定消息字段，避免创建和解析两侧直接散落 Map / JSON 字符串字段名。
     *
     * @param orderId 订单 ID
     */
    private record OrderPaidPostActionPayload(Long orderId) {

        /**
         * 反序列化本地消息 payload。
         *
         * @param payload JSON 消息体
         * @return 支付后置动作 payload
         */
        private static OrderPaidPostActionPayload fromJson(String payload) {
            return new OrderPaidPostActionPayload(JSON.parseObject(payload).getLong(ORDER_ID_PAYLOAD_FIELD));
        }

        /**
         * 序列化为本地消息 payload。
         *
         * @return JSON 消息体
         */
        private String toJson() {
            return JSON.toJSONString(Map.of(ORDER_ID_PAYLOAD_FIELD, Objects.requireNonNull(orderId, "orderId")));
        }
    }
}
