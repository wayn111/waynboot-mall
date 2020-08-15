package com.wayn.mobile.api.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.service.shop.IGoodsProductService;
import com.wayn.common.util.spring.SpringContextUtil;
import com.wayn.mobile.api.domain.Order;
import com.wayn.mobile.api.domain.OrderGoods;
import com.wayn.mobile.api.service.IOrderGoodsService;
import com.wayn.mobile.api.service.IOrderService;
import com.wayn.mobile.api.util.OrderUtil;
import com.wayn.mobile.framework.redis.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimerTask;

/**
 * 未支付订单超时自动取消任务
 */
@Slf4j
public class CancelOrderTask extends TimerTask {

    private final Long orderId;

    public CancelOrderTask(Long orderId) {
        this.orderId = orderId;
    }

    @Override
    public void run() {
        log.info("系统开始处理延时任务---订单超时未付款---" + this.orderId);
        IOrderService orderService = SpringContextUtil.getBean(IOrderService.class);
        IOrderGoodsService orderGoodsService = SpringContextUtil.getBean(IOrderGoodsService.class);
        IGoodsProductService productService = SpringContextUtil.getBean(IGoodsProductService.class);
        RedisCache redisCache = SpringContextUtil.getBean(RedisCache.class);
        Set<Long> zset = redisCache.getCacheZset("order_zset", 0, System.currentTimeMillis() / 1000);
        if (CollectionUtils.isNotEmpty(zset) && zset.contains(this.orderId)) {
            for (Long orderId : zset) {
                final Long num = redisCache.deleteZsetObject("order_zset", orderId);
                if (num != null && num > 0) {
                    Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_status", OrderUtil.STATUS_AUTO_CANCEL).eq("id", orderId));
                    if (Objects.isNull(order) || !OrderUtil.isCreateStatus(order)) {
                        return;
                    }

                    // 设置订单已取消状态
                    order.setOrderStatus(OrderUtil.STATUS_AUTO_CANCEL);
                    order.setEndTime(LocalDateTime.now());
                    if (!orderService.updateById(order)) {
                        throw new RuntimeException("更新数据已失效");
                    }

                    // 商品货品数量增加
                    Long orderId1 = order.getId();
                    List<OrderGoods> orderGoodsList = orderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_id", orderId1));
                    for (OrderGoods orderGoods : orderGoodsList) {
                        Integer productId = orderGoods.getProductId();
                        Integer number = orderGoods.getNumber();
                        if (!productService.addStock(productId, number)) {
                            throw new RuntimeException("商品货品库存增加失败");
                        }
                    }
                }
            }
        }
        log.info("系统结束处理延时任务---订单超时未付款---" + this.orderId);
    }
}
