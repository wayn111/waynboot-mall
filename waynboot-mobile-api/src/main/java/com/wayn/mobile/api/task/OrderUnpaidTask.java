package com.wayn.mobile.api.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.shop.OrderGoods;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.core.service.shop.IOrderGoodsService;
import com.wayn.common.core.util.OrderUtil;
import com.wayn.common.task.Task;
import com.wayn.common.util.spring.SpringContextUtil;
import com.wayn.data.redis.manager.RedisCache;
import com.wayn.mobile.api.service.IMobileOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 订单未支付超时自动取消任务
 */
@Slf4j
public class OrderUnpaidTask extends Task {

    /**
     * 默认延迟时间30分钟，单位毫秒
     */
    private static final long DELAY_TIME = 30 * 60 * 1000;

    /**
     * 订单id
     */
    private final Long orderId;

    public OrderUnpaidTask(Long orderId, long delayInMilliseconds) {
        super("CancelOrderTask-" + orderId, delayInMilliseconds);
        this.orderId = orderId;
    }

    public OrderUnpaidTask(Long orderId) {
        super("CancelOrderTask-" + orderId, DELAY_TIME);
        this.orderId = orderId;
    }

    @Override
    public void run() {
        log.info("系统开始处理延时任务---订单超时未付款---{}", this.orderId);
        IMobileOrderService orderService = SpringContextUtil.getBean(IMobileOrderService.class);
        IOrderGoodsService orderGoodsService = SpringContextUtil.getBean(IOrderGoodsService.class);
        IGoodsProductService productService = SpringContextUtil.getBean(IGoodsProductService.class);
        RedisCache redisCache = SpringContextUtil.getBean(RedisCache.class);
        Set<Long> zSet = redisCache.getCacheZset("order_zset", 0, System.currentTimeMillis());
        if (CollectionUtils.isEmpty(zSet) || !zSet.contains(this.orderId)) {
            return;
        }
        for (Long orderId : zSet) {
            log.info("redis内未付款订单, 编号：{} begin", orderId);
            final Long num = redisCache.deleteZsetObject("order_zset", orderId);
            if (num == null || num <= 0) {
                break;
            }
            Order order = orderService.getOne(Wrappers.lambdaQuery(Order.class)
                    .eq(Order::getOrderStatus, OrderUtil.STATUS_CREATE)
                    .eq(Order::getId, orderId));
            if (Objects.isNull(order) || !OrderUtil.isCreateStatus(order)) {
                break;
            }

            // 设置订单为已取消状态
            order.setOrderStatus(OrderUtil.STATUS_AUTO_CANCEL);
            order.setOrderEndTime(LocalDateTime.now());
            if (!orderService.updateById(order)) {
                log.info("redis内未付款订单, 编号：{} 更新订单状态失败", orderId);
                throw new RuntimeException("更新订单状态失败");
            }

            // 商品货品数量增加
            List<OrderGoods> orderGoodsList = orderGoodsService.list(Wrappers.lambdaQuery(OrderGoods.class)
                    .eq(OrderGoods::getOrderId, orderId));
            for (OrderGoods orderGoods : orderGoodsList) {
                Long productId = orderGoods.getProductId();
                Integer number = orderGoods.getNumber();
                if (!productService.addStock(productId, number)) {
                    log.info("redis内未付款订单, 编号：{} 商品货品库存增加失败", orderId);
                    throw new RuntimeException("商品货品库存增加失败");
                }
            }
            log.info("redis内未付款订单, 编号：{} end", orderId);
        }
        log.info("系统结束处理延时任务---订单超时未付款---{}", this.orderId);
    }
}
