package com.wayn.admin.api.schedule;

import com.wayn.domain.inventory.service.impl.InventoryReconciliationService;
import com.wayn.domain.api.inventory.service.InventoryReconciliationSummary;
import com.wayn.domain.api.trade.service.PaymentReconciliationQuery;
import com.wayn.domain.api.trade.service.PaymentReconciliationService;
import com.wayn.domain.api.trade.service.PaymentReconciliationSummary;
import com.wayn.domain.inventory.support.RedisStockSnapshotRefreshResult;
import com.wayn.domain.inventory.support.RedisStockSnapshotSupport;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 交易治理定时任务。
 * 承接 Redis 库存快照预热、库存流水对账修复和支付日终对账，避免核心下单/支付链路承担补偿扫描压力。
 * 与管理端 {@code TradeOpsController} 共用同一份依赖；执行异常由 Spring 默认 {@code TaskUtils.LoggingErrorHandler} 记录并继续下次调度。
 */
@Slf4j
@Component
@AllArgsConstructor
public class TradeGovernanceScheduledTask {

    private final RedisStockSnapshotSupport redisStockSnapshotSupport;
    private final InventoryReconciliationService inventoryReconciliationService;
    private final PaymentReconciliationService paymentReconciliationService;
    private final TradeScheduleProperties properties;

    /**
     * 刷新 Redis 库存快照。
     */
    @Scheduled(cron = "${wayn.schedule.trade.stock-snapshot.cron:0 */5 * * * *}")
    public void refreshStockSnapshot() {
        TradeScheduleProperties.StockSnapshot cfg = properties.getStockSnapshot();
        List<RedisStockSnapshotRefreshResult> results = redisStockSnapshotSupport
                .refreshLatestSnapshots(cfg.getLimit(), cfg.getBucketCount());
        log.info("trade_stock_snapshot_refresh_done limit={} bucketCount={} count={}",
                cfg.getLimit(), cfg.getBucketCount(), results.size());
    }

    /**
     * 库存流水对账。repair=true 时自动把 locked_stock 修复为流水推导值。
     */
    @Scheduled(cron = "${wayn.schedule.trade.inventory-reconcile.cron:0 0 * * * *}")
    public void reconcileInventory() {
        TradeScheduleProperties.InventoryReconcile cfg = properties.getInventoryReconcile();
        InventoryReconciliationSummary summary = inventoryReconciliationService
                .reconcileLatestLockedStock(cfg.getLimit(), cfg.isRepair(), "scheduler");
        log.info("trade_inventory_reconcile_done checked={} mismatch={} repaired={}",
                summary.getCheckedProductCount(), summary.getMismatchCount(), summary.getRepairedCount());
    }

    /**
     * 支付日终对账。差异由 {@code TradeOpsController} 介入处理。
     */
    @Scheduled(cron = "${wayn.schedule.trade.payment-reconcile.cron:0 0 2 * * *}")
    public void reconcilePaymentDaily() {
        PaymentReconciliationSummary summary = paymentReconciliationService
                .reconcile(PaymentReconciliationQuery.defaultQuery());
        log.info("trade_payment_daily_reconcile_done difference={} paymentFlow={} channelBill={} refundFlow={}",
                summary.getDifferenceCount(), summary.getPaymentFlowCount(),
                summary.getChannelBillCount(), summary.getRefundFlowCount());
    }
}
