package com.wayn.common.core.service.shop.support.payment;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.shop.OrderGoods;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 支付成功后的后置动作支撑服务。
 * 当前只负责更新虚拟销量，后续可继续挂接积分、通知等支付成功副作用。
 */
@Slf4j
@Service
@AllArgsConstructor
public class PaymentPostActionSupport {

    private final IOrderGoodsService orderGoodsService;
    private final IGoodsService goodsService;
    private final ThreadPoolTaskExecutor commonThreadPoolTaskExecutor;

    /**
     * 处理支付成功后的后置动作。
     * 虚拟销量更新不参与支付状态事务，异步执行可以缩短支付回调占用时间，失败只记录日志后续人工补偿。
     *
     * @param orderId 订单 ID
     */
    public void handleOrderPaid(Long orderId) {
        commonThreadPoolTaskExecutor.execute(() -> updateVirtualSales(orderId));
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
        }
    }
}
