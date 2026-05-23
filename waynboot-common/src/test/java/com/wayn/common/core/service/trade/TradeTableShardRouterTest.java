package com.wayn.common.core.service.trade;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradeTableShardRouterTest {

    @Test
    void routeOrderTablesByCreateMonth() {
        TradeTableShardRouter router = new TradeTableShardRouter();

        TradeTableRoute route = router.routeOrderTables(YearMonth.of(2026, 5));

        assertEquals("shop_order_202605", route.getOrderTable());
        assertEquals("shop_order_goods_202605", route.getOrderGoodsTable());
        assertEquals("shop_payment_flow_202605", route.getPaymentFlowTable());
        assertEquals("local_message_202605", route.getLocalMessageTable());
    }
}
