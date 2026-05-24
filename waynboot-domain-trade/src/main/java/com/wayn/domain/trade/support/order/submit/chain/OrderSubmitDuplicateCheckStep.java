package com.wayn.domain.trade.support.order.submit.chain;

import com.wayn.domain.trade.support.order.OrderSubmitPersistenceSupport;
import com.wayn.message.core.dto.OrderDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单查重责任链步骤。
 * 作为下单链路的第一道副作用保护，防止 MQ 重投、批量重试或并发消费导致重复扣库存和重复落库。
 */
@Slf4j
@Component
@AllArgsConstructor
public class OrderSubmitDuplicateCheckStep implements OrderSubmitStep {

    private final OrderSubmitPersistenceSupport orderSubmitPersistenceSupport;

    /**
     * 返回订单查重步骤顺序。
     *
     * @return 步骤顺序
     */
    @Override
    public int order() {
        return DUPLICATE_CHECK_ORDER;
    }

    /**
     * 检查订单号是否已经落库。
     * 已存在订单会中断责任链，外层批量链路会把该订单视为成功消费。
     *
     * @param context 下单责任链上下文
     */
    @Override
    public void execute(OrderSubmitChainContext context) {
        OrderDTO orderDTO = context.getOrderDTO();
        if (!orderSubmitPersistenceSupport.existsOrder(orderDTO.getOrderSn())) {
            // 未落库订单继续进入上下文构建和库存扣减步骤。
            return;
        }
        log.info("订单已存在，跳过重复落单, orderSn={}, userId={}", orderDTO.getOrderSn(), orderDTO.getUserId());
        // 已落库订单说明历史消费已完成，本次重复消息不能再触发库存扣减和订单写入。
        context.markExistingOrder();
        context.stop("订单已存在");
    }
}
