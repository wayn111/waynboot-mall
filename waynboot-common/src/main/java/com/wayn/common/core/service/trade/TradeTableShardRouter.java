package com.wayn.common.core.service.trade;

import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * 交易表分片路由器。
 * 先沉淀订单链路按月分表的命名和路由规则，后续接入 ShardingSphere 或自研路由时复用同一规则。
 */
@Component
public class TradeTableShardRouter {

    private static final DateTimeFormatter SHARD_SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 按订单创建月份路由交易核心表。
     *
     * @param createMonth 订单创建月份
     * @return 交易表路由结果
     */
    public TradeTableRoute routeOrderTables(YearMonth createMonth) {
        YearMonth safeMonth = createMonth == null ? YearMonth.now() : createMonth;
        String suffix = safeMonth.format(SHARD_SUFFIX_FORMATTER);
        TradeTableRoute route = new TradeTableRoute();
        route.setOrderTable("shop_order_" + suffix);
        route.setOrderGoodsTable("shop_order_goods_" + suffix);
        route.setPaymentFlowTable("shop_payment_flow_" + suffix);
        route.setLocalMessageTable("local_message_" + suffix);
        return route;
    }
}
