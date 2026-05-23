package com.wayn.admin.api.controller.ops;

import com.wayn.common.core.entity.message.LocalMessage;
import com.wayn.common.core.service.message.LocalMessageCompensationMetric;
import com.wayn.common.core.service.message.LocalMessageCompensationService;
import com.wayn.common.core.service.shop.InventoryReconciliationDifference;
import com.wayn.common.core.service.shop.InventoryReconciliationService;
import com.wayn.common.core.service.shop.InventoryReconciliationSummary;
import com.wayn.common.core.service.shop.PaymentReconciliationDifference;
import com.wayn.common.core.service.shop.PaymentReconciliationQuery;
import com.wayn.common.core.service.shop.PaymentReconciliationService;
import com.wayn.common.core.service.shop.PaymentReconciliationSummary;
import com.wayn.common.core.service.shop.support.order.RedisStockSnapshotRefreshResult;
import com.wayn.common.core.service.shop.support.order.RedisStockSnapshotSupport;
import com.wayn.common.model.request.InventoryReconciliationReqVO;
import com.wayn.common.model.request.PaymentReconciliationReqVO;
import com.wayn.common.model.request.StockSnapshotRefreshReqVO;
import com.wayn.common.model.response.InventoryReconciliationDifferenceResVO;
import com.wayn.common.model.response.InventoryReconciliationSummaryResVO;
import com.wayn.common.model.response.LocalMessageCompensationResVO;
import com.wayn.common.model.response.LocalMessageCompensationMetricResVO;
import com.wayn.common.model.response.PaymentReconciliationDifferenceResVO;
import com.wayn.common.model.response.PaymentReconciliationSummaryResVO;
import com.wayn.common.model.response.RedisStockSnapshotRefreshResVO;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 交易运营治理接口。
 * 提供本地消息补偿和支付基础对账能力，帮助管理端处理最终一致性异常。
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("ops/trade")
public class TradeOpsController {

    private final LocalMessageCompensationService localMessageCompensationService;
    private final PaymentReconciliationService paymentReconciliationService;
    private final RedisStockSnapshotSupport redisStockSnapshotSupport;
    private final InventoryReconciliationService inventoryReconciliationService;

    /**
     * 查询失败本地消息。
     *
     * @param limit 查询数量
     * @return 失败本地消息列表
     */
    @PreAuthorize("@ss.hasPermi('ops:trade:message:list')")
    @GetMapping("local-message/failed")
    public R<List<LocalMessageCompensationResVO>> listFailedLocalMessages(
            @RequestParam(defaultValue = "50") Integer limit) {
        List<LocalMessageCompensationResVO> result = localMessageCompensationService.listFailedMessages(limit)
                .stream()
                .map(this::toLocalMessageVO)
                .toList();
        log.info("查询失败本地消息完成, limit={}, count={}", limit, result.size());
        return R.success(result);
    }

    /**
     * 人工重投失败本地消息。
     *
     * @param messageId 本地消息 ID
     * @param operator 操作者
     * @return true=重投状态重置成功
     */
    @PreAuthorize("@ss.hasPermi('ops:trade:message:retry')")
    @PostMapping("local-message/{messageId}/retry")
    public R<Boolean> retryFailedLocalMessage(@PathVariable Long messageId,
                                              @RequestParam(defaultValue = "admin") String operator) {
        boolean retried = localMessageCompensationService.retryFailedMessage(messageId, operator);
        log.info("人工重投本地消息完成, messageId={}, operator={}, result={}", messageId, operator, retried);
        return R.result(retried);
    }

    /**
     * 查询本地消息补偿指标。
     *
     * @return 本地消息补偿指标
     */
    @PreAuthorize("@ss.hasPermi('ops:trade:message:metric')")
    @GetMapping("local-message/metric")
    public R<LocalMessageCompensationMetricResVO> countLocalMessageMetric() {
        LocalMessageCompensationMetric metric = localMessageCompensationService.countFailedMessages();
        LocalMessageCompensationMetricResVO resVO = new LocalMessageCompensationMetricResVO();
        resVO.setFailedCount(metric.getFailedCount());
        log.info("查询本地消息补偿指标完成, failedCount={}", metric.getFailedCount());
        return R.success(resVO);
    }

    /**
     * 刷新 Redis 库存快照。
     *
     * @param reqVO Redis 库存快照刷新请求
     * @return 刷新结果列表
     */
    @PreAuthorize("@ss.hasPermi('ops:trade:stock:snapshot')")
    @PostMapping("stock/snapshot/refresh")
    public R<List<RedisStockSnapshotRefreshResVO>> refreshStockSnapshot(@RequestBody(required = false)
                                                                        StockSnapshotRefreshReqVO reqVO) {
        StockSnapshotRefreshReqVO safeReqVO = reqVO == null ? new StockSnapshotRefreshReqVO() : reqVO;
        int bucketCount = safeReqVO.getBucketCount() == null ? 1 : safeReqVO.getBucketCount();
        List<RedisStockSnapshotRefreshResult> results;
        if (CollectionUtils.isEmpty(safeReqVO.getProductIds())) {
            int limit = safeReqVO.getLimit() == null ? 500 : safeReqVO.getLimit();
            results = redisStockSnapshotSupport.refreshLatestSnapshots(limit, bucketCount);
        } else {
            results = redisStockSnapshotSupport.refreshProductSnapshots(safeReqVO.getProductIds(), bucketCount);
        }
        log.info("刷新Redis库存快照完成, productIds={}, bucketCount={}, count={}",
                safeReqVO.getProductIds(), bucketCount, results.size());
        return R.success(results.stream().map(this::toRedisStockSnapshotVO).toList());
    }

    /**
     * 执行库存流水对账。
     *
     * @param reqVO 库存流水对账请求
     * @return 库存对账汇总
     */
    @PreAuthorize("@ss.hasPermi('ops:trade:inventory:reconcile')")
    @PostMapping("inventory/reconcile")
    public R<InventoryReconciliationSummaryResVO> reconcileInventory(@RequestBody(required = false)
                                                                     InventoryReconciliationReqVO reqVO) {
        InventoryReconciliationReqVO safeReqVO = reqVO == null ? new InventoryReconciliationReqVO() : reqVO;
        boolean repair = Boolean.TRUE.equals(safeReqVO.getRepair());
        InventoryReconciliationSummary summary;
        if (CollectionUtils.isEmpty(safeReqVO.getProductIds())) {
            int limit = safeReqVO.getLimit() == null ? 500 : safeReqVO.getLimit();
            summary = inventoryReconciliationService.reconcileLatestLockedStock(limit, repair, "admin");
        } else if (repair) {
            summary = inventoryReconciliationService.reconcileAndRepairLockedStock(safeReqVO.getProductIds(), "admin");
        } else {
            summary = inventoryReconciliationService.reconcileLockedStock(safeReqVO.getProductIds());
        }
        log.info("库存流水对账完成, repair={}, checked={}, mismatch={}, repaired={}",
                repair, summary.getCheckedProductCount(), summary.getMismatchCount(), summary.getRepairedCount());
        return R.success(toInventoryReconciliationVO(summary));
    }

    /**
     * 执行支付基础对账。
     *
     * @param reqVO 支付对账查询请求
     * @return 支付对账汇总
     */
    @PreAuthorize("@ss.hasPermi('ops:trade:payment:reconcile')")
    @PostMapping("payment/reconcile")
    public R<PaymentReconciliationSummaryResVO> reconcilePayment(@RequestBody(required = false)
                                                                 PaymentReconciliationReqVO reqVO) {
        PaymentReconciliationReqVO safeReqVO = reqVO == null ? new PaymentReconciliationReqVO() : reqVO;
        PaymentReconciliationSummary summary = paymentReconciliationService.reconcile(new PaymentReconciliationQuery(
                safeReqVO.getStartTime(), safeReqVO.getEndTime(), safeReqVO.getLimit()));
        log.info("支付对账完成, startTime={}, endTime={}, differenceCount={}",
                safeReqVO.getStartTime(), safeReqVO.getEndTime(), summary.getDifferenceCount());
        return R.success(toPaymentReconciliationVO(summary));
    }

    /**
     * 转换本地消息补偿响应 VO。
     *
     * @param message 本地消息
     * @return 本地消息补偿响应 VO
     */
    private LocalMessageCompensationResVO toLocalMessageVO(LocalMessage message) {
        LocalMessageCompensationResVO resVO = new LocalMessageCompensationResVO();
        resVO.setId(message.getId());
        resVO.setMessageKey(message.getMessageKey());
        resVO.setTopic(message.getTopic());
        resVO.setBizType(message.getBizType());
        resVO.setBizId(message.getBizId());
        resVO.setRetryCount(message.getRetryCount());
        resVO.setNextRetryTime(message.getNextRetryTime());
        resVO.setLastError(message.getLastError());
        resVO.setUpdateTime(message.getUpdateTime());
        return resVO;
    }

    /**
     * 转换支付对账汇总响应 VO。
     *
     * @param summary 支付对账汇总
     * @return 支付对账汇总响应 VO
     */
    private PaymentReconciliationSummaryResVO toPaymentReconciliationVO(PaymentReconciliationSummary summary) {
        PaymentReconciliationSummaryResVO resVO = new PaymentReconciliationSummaryResVO();
        resVO.setPaymentFlowCount(summary.getPaymentFlowCount());
        resVO.setPaidOrderCount(summary.getPaidOrderCount());
        resVO.setDifferenceCount(summary.getDifferenceCount());
        resVO.setAmountMismatchCount(summary.getAmountMismatchCount());
        resVO.setMissingPaymentFlowCount(summary.getMissingPaymentFlowCount());
        resVO.setMissingOrderCount(summary.getMissingOrderCount());
        resVO.setOrderStatusMismatchCount(summary.getOrderStatusMismatchCount());
        resVO.setChannelBillCount(summary.getChannelBillCount());
        resVO.setRefundFlowCount(summary.getRefundFlowCount());
        resVO.setChannelBillMismatchCount(summary.getChannelBillMismatchCount());
        resVO.setRefundFlowMismatchCount(summary.getRefundFlowMismatchCount());
        resVO.setDifferences(summary.getDifferences().stream()
                .map(this::toPaymentReconciliationDifferenceVO)
                .toList());
        return resVO;
    }

    /**
     * 转换支付对账差异响应 VO。
     *
     * @param difference 支付对账差异
     * @return 支付对账差异响应 VO
     */
    private PaymentReconciliationDifferenceResVO toPaymentReconciliationDifferenceVO(
            PaymentReconciliationDifference difference) {
        PaymentReconciliationDifferenceResVO resVO = new PaymentReconciliationDifferenceResVO();
        resVO.setDifferenceType(difference.getDifferenceType());
        resVO.setOrderSn(difference.getOrderSn());
        resVO.setFlowAmount(difference.getFlowAmount());
        resVO.setOrderAmount(difference.getOrderAmount());
        resVO.setMessage(difference.getMessage());
        return resVO;
    }

    /**
     * 转换 Redis 库存快照刷新响应 VO。
     *
     * @param result 刷新结果
     * @return Redis 库存快照刷新响应 VO
     */
    private RedisStockSnapshotRefreshResVO toRedisStockSnapshotVO(RedisStockSnapshotRefreshResult result) {
        RedisStockSnapshotRefreshResVO resVO = new RedisStockSnapshotRefreshResVO();
        resVO.setProductId(result.getProductId());
        resVO.setAvailableStock(result.getAvailableStock());
        resVO.setBucketCount(result.getBucketCount());
        resVO.setRefreshedBucketCount(result.getRefreshedBucketCount());
        return resVO;
    }

    /**
     * 转换库存对账汇总响应 VO。
     *
     * @param summary 库存对账汇总
     * @return 库存对账汇总响应 VO
     */
    private InventoryReconciliationSummaryResVO toInventoryReconciliationVO(InventoryReconciliationSummary summary) {
        InventoryReconciliationSummaryResVO resVO = new InventoryReconciliationSummaryResVO();
        resVO.setCheckedProductCount(summary.getCheckedProductCount());
        resVO.setMismatchCount(summary.getMismatchCount());
        resVO.setRepairedCount(summary.getRepairedCount());
        resVO.setDifferences(summary.getDifferences().stream()
                .map(this::toInventoryReconciliationDifferenceVO)
                .toList());
        return resVO;
    }

    /**
     * 转换库存对账差异响应 VO。
     *
     * @param difference 库存对账差异
     * @return 库存对账差异响应 VO
     */
    private InventoryReconciliationDifferenceResVO toInventoryReconciliationDifferenceVO(
            InventoryReconciliationDifference difference) {
        InventoryReconciliationDifferenceResVO resVO = new InventoryReconciliationDifferenceResVO();
        resVO.setProductId(difference.getProductId());
        resVO.setExpectedLockedStock(difference.getExpectedLockedStock());
        resVO.setActualLockedStock(difference.getActualLockedStock());
        resVO.setMessage(difference.getMessage());
        resVO.setRepaired(difference.isRepaired());
        return resVO;
    }
}
