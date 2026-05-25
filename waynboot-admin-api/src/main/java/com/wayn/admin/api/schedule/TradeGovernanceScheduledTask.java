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
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 交易治理定时任务。
 * 承接 Redis 库存快照预热、库存流水对账修复和支付日终对账，避免核心下单/支付链路承担补偿扫描压力。
 * 与管理端 {@code TradeOpsController} 共用同一份依赖；执行异常由 Spring 默认 {@code TaskUtils.LoggingErrorHandler} 记录并继续下次调度。
 *
 * <h3>分布式锁说明（ShedLock）</h3>
 * 所有 {@link Scheduled} 方法均通过 {@link SchedulerLock} 在 Redis 中互斥执行，避免 admin 多实例部署时同一任务被重复触发。
 * 锁前缀来自 {@code ShedLockConfig#lockProvider} 中的 {@code "waynboot-mall"} 命名空间。
 *
 * <p>{@code SchedulerLock} 三个核心参数语义：
 * <ul>
 *   <li><b>name</b>：锁名（即 Redis Key 的业务部分），同一 name 的所有节点互斥；不同任务必须用不同 name，否则会互相阻塞。</li>
 *   <li><b>lockAtMostFor</b>：锁的最长持有时间（ISO-8601 时长，如 {@code PT30M}）。即使持锁节点崩溃未释放锁，到期后其他节点也能接管，避免任务永久卡死。设置原则：略大于任务正常最坏耗时；过短会导致长任务被并发执行，过长会让崩溃恢复延迟。</li>
 *   <li><b>lockAtLeastFor</b>：锁的最短持有时间。任务即使秒级完成，锁也至少保持这么久，防止时钟漂移让两个节点几乎同时运行。设置原则：略大于集群间最大时钟差；通常 {@code PT30S} ~ {@code PT1M} 即可。</li>
 * </ul>
 *
 * <p>当前任务的取值依据：
 * <ul>
 *   <li>{@code refreshStockSnapshot}：每 5 分钟一次，单次仅刷新少量热点桶，正常秒级；{@code lockAtMostFor=PT4M} 留有 1 分钟缓冲且不挤占下一次；{@code lockAtLeastFor=PT30S} 兜底时钟漂移。</li>
 *   <li>{@code reconcileInventory}：每小时一次，全表对账，可能数分钟；{@code lockAtMostFor=PT30M} 给极端慢查询留余量；{@code lockAtLeastFor=PT1M} 防止短路完成后被别的节点立刻再跑。</li>
 *   <li>{@code reconcilePaymentDaily}：日终大对账，最长可能数十分钟；{@code lockAtMostFor=PT1H} 兜底；{@code lockAtLeastFor=PT1M} 防重入。</li>
 * </ul>
 *
 * <p>类级 {@code @EnableSchedulerLock(defaultLockAtMostFor = "PT10M")} 配置在 {@code AdminApplication}，作为漏配方法的兜底。
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
     * 默认每 5 分钟执行一次，用于把 MySQL SKU 库存预热进 Redis 入口闸门，避免热点 SKU 每次下单都要回查数据库。
     *
     * <pre>
     * ┌──────────────────────────────────────────────────────────────────────┐
     * │ ShedLock 抢锁（Key=waynboot-mall:tradeStockSnapshotRefreshJob）       │
     * │   ├─ 抢到 → 继续                                                      │
     * │   └─ 未抢到 → 直接结束（其他 admin 实例正在执行）                       │
     * ├──────────────────────────────────────────────────────────────────────┤
     * │ 读配置 wayn.schedule.trade.stock-snapshot.{limit,bucket-count}        │
     * ├──────────────────────────────────────────────────────────────────────┤
     * │ RedisStockSnapshotSupport.refreshLatestSnapshots(limit, bucketCount) │
     * │   ├─ 按 update_time DESC 拉最近 limit 条 SKU                          │
     * │   ├─ 逐 SKU 计算 (total_stock - locked_stock) 写入 Redis 桶           │
     * │   └─ 热点桶按 bucketCount 拆分，分散并发写入压力                      │
     * ├──────────────────────────────────────────────────────────────────────┤
     * │ INFO 日志：trade_stock_snapshot_refresh_done count=N                  │
     * └──────────────────────────────────────────────────────────────────────┘
     * </pre>
     */
    @Scheduled(cron = "${wayn.schedule.trade.stock-snapshot.cron:0 */5 * * * *}")
    @SchedulerLock(name = "tradeStockSnapshotRefreshJob", lockAtMostFor = "PT4M", lockAtLeastFor = "PT30S")
    public void refreshStockSnapshot() {
        TradeScheduleProperties.StockSnapshot cfg = properties.getStockSnapshot();
        List<RedisStockSnapshotRefreshResult> results = redisStockSnapshotSupport
                .refreshLatestSnapshots(cfg.getLimit(), cfg.getBucketCount());
        log.info("trade_stock_snapshot_refresh_done limit={} bucketCount={} count={}",
                cfg.getLimit(), cfg.getBucketCount(), results.size());
    }

    /**
     * 库存流水对账。默认每小时执行一次，repair=true 时自动把 locked_stock 修复为流水推导值。
     * 用于发现"流水已写但 locked_stock 漂移"或"locked_stock 多扣未释放"等异常，由库存流水作为账本推导真值。
     *
     * <pre>
     * ┌──────────────────────────────────────────────────────────────────────┐
     * │ ShedLock 抢锁（Key=waynboot-mall:tradeInventoryReconcileJob）         │
     * ├──────────────────────────────────────────────────────────────────────┤
     * │ 读配置 wayn.schedule.trade.inventory-reconcile.{limit,repair}         │
     * ├──────────────────────────────────────────────────────────────────────┤
     * │ InventoryReconciliationService.reconcileLatestLockedStock            │
     * │   ├─ 取 update_time DESC 最近 limit 个 productId                      │
     * │   ├─ 用 inventory_flow 累加（预占+/释放-/出库-）推导期望 locked_stock │
     * │   ├─ 与 goods_product.locked_stock 比对                              │
     * │   │   ├─ 一致 → 跳过                                                 │
     * │   │   └─ 不一致 → 记入 mismatch                                      │
     * │   └─ repair=true：把 locked_stock 修正为流水推导值（不改可售库存）   │
     * ├──────────────────────────────────────────────────────────────────────┤
     * │ INFO 日志：checked / mismatch / repaired                              │
     * └──────────────────────────────────────────────────────────────────────┘
     * </pre>
     */
    @Scheduled(cron = "${wayn.schedule.trade.inventory-reconcile.cron:0 0 * * * *}")
    @SchedulerLock(name = "tradeInventoryReconcileJob", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void reconcileInventory() {
        TradeScheduleProperties.InventoryReconcile cfg = properties.getInventoryReconcile();
        InventoryReconciliationSummary summary = inventoryReconciliationService
                .reconcileLatestLockedStock(cfg.getLimit(), cfg.isRepair(), "scheduler");
        log.info("trade_inventory_reconcile_done checked={} mismatch={} repaired={}",
                summary.getCheckedProductCount(), summary.getMismatchCount(), summary.getRepairedCount());
    }

    /**
     * 支付日终对账。默认每天 02:00 执行，对照支付流水/渠道账单/退款流水/订单四个数据源的金额一致性。
     * 差异不会自动修复，由 {@code TradeOpsController} 暴露的治理接口由人工介入处理。
     *
     * <pre>
     * ┌──────────────────────────────────────────────────────────────────────┐
     * │ ShedLock 抢锁（Key=waynboot-mall:tradePaymentDailyReconcileJob）      │
     * ├──────────────────────────────────────────────────────────────────────┤
     * │ PaymentReconciliationQuery.defaultQuery() （默认昨天 00:00 ~ 24:00） │
     * ├──────────────────────────────────────────────────────────────────────┤
     * │ PaymentReconciliationService.reconcile                                │
     * │   ├─ 维度1：支付流水 vs 订单                                          │
     * │   │     ├─ 流水无订单 → FLOW_WITHOUT_ORDER                           │
     * │   │     ├─ 订单非已支付 → ORDER_STATUS_MISMATCH                      │
     * │   │     └─ 金额不一致 → AMOUNT_MISMATCH                              │
     * │   ├─ 维度2：已支付订单 vs 流水（反向兜底） → ORDER_WITHOUT_FLOW      │
     * │   ├─ 维度3：渠道账单 vs 内部流水                                      │
     * │   │     ├─ 缺流水 → CHANNEL_BILL_WITHOUT_FLOW                        │
     * │   │     └─ 金额不一致 → CHANNEL_AMOUNT_MISMATCH                     │
     * │   └─ 维度4：退款流水 vs 订单退款                                      │
     * │         ├─ 退款流水无订单 → REFUND_FLOW_WITHOUT_ORDER                │
     * │         └─ 金额不一致 → REFUND_AMOUNT_MISMATCH                      │
     * ├──────────────────────────────────────────────────────────────────────┤
     * │ INFO 日志：difference / paymentFlow / channelBill / refundFlow        │
     * │ 差异 → TradeOpsController 介入处理（不自动修复）                     │
     * └──────────────────────────────────────────────────────────────────────┘
     * </pre>
     */
    @Scheduled(cron = "${wayn.schedule.trade.payment-reconcile.cron:0 0 2 * * *}")
    @SchedulerLock(name = "tradePaymentDailyReconcileJob", lockAtMostFor = "PT1H", lockAtLeastFor = "PT1M")
    public void reconcilePaymentDaily() {
        PaymentReconciliationSummary summary = paymentReconciliationService
                .reconcile(PaymentReconciliationQuery.defaultQuery());
        log.info("trade_payment_daily_reconcile_done difference={} paymentFlow={} channelBill={} refundFlow={}",
                summary.getDifferenceCount(), summary.getPaymentFlowCount(),
                summary.getChannelBillCount(), summary.getRefundFlowCount());
    }
}
