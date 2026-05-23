package com.wayn.admin.api.schedule;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 交易治理定时任务配置。
 * 集中表达调度周期与批量参数，新人改频率/批量只需要看一处，不必再翻 schedule 类源码。
 * <p>
 * 配置前缀：{@code wayn.schedule.trade}。
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "wayn.schedule.trade")
public class TradeScheduleProperties {

    /** Redis 库存快照预热。默认每 5 分钟一次，仅在热点 SKU 场景下需要 bucketCount > 1。 */
    private final StockSnapshot stockSnapshot = new StockSnapshot();

    /** 库存流水对账。默认每小时整点扫描一批，发现差异时按 repair 决定是否自动修复。 */
    private final InventoryReconcile inventoryReconcile = new InventoryReconcile();

    /** 支付日终对账。默认每天 02:00 触发，差异需要人工介入处理。 */
    private final PaymentReconcile paymentReconcile = new PaymentReconcile();

    @Getter
    @Setter
    public static class StockSnapshot {
        private String cron = "0 */5 * * * *";
        private int limit = 500;
        private int bucketCount = 1;
    }

    @Getter
    @Setter
    public static class InventoryReconcile {
        private String cron = "0 0 * * * *";
        private int limit = 500;
        private boolean repair = true;
    }

    @Getter
    @Setter
    public static class PaymentReconcile {
        private String cron = "0 0 2 * * *";
    }
}
