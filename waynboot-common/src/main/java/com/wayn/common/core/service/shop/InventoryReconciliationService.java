package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.common.core.entity.shop.GoodsProduct;
import com.wayn.common.core.entity.shop.InventoryFlow;
import com.wayn.common.core.enums.InventoryChangeTypeEnum;
import com.wayn.common.core.mapper.shop.InventoryFlowMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 库存流水对账服务。
 * 以库存流水为账本推导 SKU 冻结库存，和商品货品表 locked_stock 做比对；自动修复仅修正 locked_stock，不改可售库存。
 */
@Slf4j
@Service
@AllArgsConstructor
public class InventoryReconciliationService {

    private static final int DEFAULT_RECONCILE_LIMIT = 500;
    private static final String SYSTEM_OPERATOR = "system";
    private static final String LOCKED_STOCK_MISMATCH_MESSAGE = "冻结库存与库存流水不一致";

    private final IGoodsProductService goodsProductService;
    private final InventoryFlowMapper inventoryFlowMapper;

    /**
     * 对账指定 SKU 的冻结库存。
     *
     * @param productIds 商品货品 ID 集合
     * @return 对账汇总
     */
    public InventoryReconciliationSummary reconcileLockedStock(List<Long> productIds) {
        return doReconcileLockedStock(productIds, false, SYSTEM_OPERATOR);
    }

    /**
     * 对账并自动修复指定 SKU 的冻结库存。
     * 该方法只把 locked_stock 修正为流水推导值，用于补偿确认、释放、退款等链路出现异常后的兜底修复。
     *
     * @param productIds 商品货品 ID 集合
     * @param operator 操作者
     * @return 对账汇总
     */
    public InventoryReconciliationSummary reconcileAndRepairLockedStock(List<Long> productIds, String operator) {
        return doReconcileLockedStock(productIds, true, operator);
    }

    /**
     * 对账最近更新的一批 SKU 冻结库存。
     * 定时任务可按批次循环执行，避免一次性全表扫描影响商品库。
     *
     * @param limit 扫描数量
     * @param repair 是否自动修复
     * @param operator 操作者
     * @return 对账汇总
     */
    public InventoryReconciliationSummary reconcileLatestLockedStock(int limit, boolean repair, String operator) {
        return doReconcileLockedStock(listLatestProductIds(limit), repair, operator);
    }

    /**
     * 执行冻结库存对账。
     *
     * @param productIds 商品货品 ID 集合
     * @param repair 是否自动修复
     * @param operator 操作者
     * @return 对账汇总
     */
    private InventoryReconciliationSummary doReconcileLockedStock(List<Long> productIds, boolean repair,
                                                                  String operator) {
        InventoryReconciliationSummary summary = new InventoryReconciliationSummary();
        List<Long> distinctProductIds = normalizeProductIds(productIds);
        if (CollectionUtils.isEmpty(distinctProductIds)) {
            return summary;
        }
        List<GoodsProduct> products = goodsProductService.listByIds(distinctProductIds);
        summary.setCheckedProductCount(products.size());
        if (CollectionUtils.isEmpty(products)) {
            // 传入的 SKU 已不存在时，不继续扫描流水；缺失商品属于数据治理问题，不应生成库存差异误导自动修复。
            return summary;
        }
        Map<Long, Integer> expectedLockedStockMap = calculateExpectedLockedStock(distinctProductIds);
        for (GoodsProduct product : products) {
            reconcileProduct(summary, product, expectedLockedStockMap, repair, operator);
        }
        return summary;
    }

    /**
     * 查询最近更新的商品货品 ID。
     * 定时任务只需要 ID 参与后续流水对账，避免把完整 SKU 数据提前加载到内存。
     *
     * @param limit 扫描数量
     * @return 商品货品 ID 列表
     */
    private List<Long> listLatestProductIds(int limit) {
        return CollectionUtils.emptyIfNull(goodsProductService.list(Wrappers.lambdaQuery(GoodsProduct.class)
                        .select(GoodsProduct::getId)
                        .orderByDesc(GoodsProduct::getUpdateTime)
                        .last(limitClause(limit))))
                .stream()
                .filter(Objects::nonNull)
                .map(GoodsProduct::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 清洗对账输入 SKU。
     * 外部接口和定时任务可能传入重复或空 ID；统一在入口收敛，避免后续生成无意义 SQL 条件。
     *
     * @param productIds 原始商品货品 ID 集合
     * @return 去重后的非空商品货品 ID 集合
     */
    private List<Long> normalizeProductIds(List<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return List.of();
        }
        return productIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 对账单个商品货品的冻结库存。
     * 单 SKU 维度只负责比对和可选修复，汇总计数仍由 summary 统一维护。
     *
     * @param summary 对账汇总
     * @param product 商品货品
     * @param expectedLockedStockMap 流水推导的冻结库存
     * @param repair 是否自动修复
     * @param operator 操作者
     */
    private void reconcileProduct(InventoryReconciliationSummary summary, GoodsProduct product,
                                  Map<Long, Integer> expectedLockedStockMap, boolean repair, String operator) {
        int expectedLockedStock = resolveExpectedLockedStock(product.getId(), expectedLockedStockMap);
        int actualLockedStock = defaultNumber(product.getLockedStock());
        if (expectedLockedStock == actualLockedStock) {
            return;
        }
        InventoryReconciliationDifference difference = buildDifference(product.getId(), expectedLockedStock,
                actualLockedStock);
        if (repair && repairLockedStock(product.getId(), expectedLockedStock, actualLockedStock, operator)) {
            difference.setRepaired(true);
            summary.incrementRepairedCount();
        }
        summary.addDifference(difference);
    }

    /**
     * 解析流水推导冻结库存。
     * 历史异常流水可能把冻结库存推导成负数，对账结果按业务含义收敛到 0，避免自动修复写入非法库存。
     *
     * @param productId 商品货品 ID
     * @param expectedLockedStockMap 流水推导的冻结库存
     * @return 非负冻结库存
     */
    private int resolveExpectedLockedStock(Long productId, Map<Long, Integer> expectedLockedStockMap) {
        return Math.max(0, expectedLockedStockMap.getOrDefault(productId, 0));
    }

    /**
     * 通过库存流水推导当前冻结库存。
     * 冻结增加 locked_stock，支付确认和取消释放都会减少 locked_stock，退款回补不影响冻结库存。
     *
     * @param productIds 商品货品 ID 集合
     * @return productId 到冻结库存推导值的映射
     */
    private Map<Long, Integer> calculateExpectedLockedStock(List<Long> productIds) {
        List<InventoryFlow> flows = inventoryFlowMapper.selectList(Wrappers.lambdaQuery(InventoryFlow.class)
                .in(InventoryFlow::getProductId, productIds));
        Map<Long, Integer> expectedLockedStockMap = new HashMap<>();
        for (InventoryFlow flow : CollectionUtils.emptyIfNull(flows)) {
            if (flow == null || flow.getProductId() == null) {
                continue;
            }
            int delta = calculateLockedStockDelta(flow);
            expectedLockedStockMap.merge(flow.getProductId(), delta, Integer::sum);
        }
        return expectedLockedStockMap;
    }

    /**
     * 计算单条流水对冻结库存的影响。
     *
     * @param flow 库存流水
     * @return 冻结库存增量
     */
    private int calculateLockedStockDelta(InventoryFlow flow) {
        int changeNumber = defaultNumber(flow.getChangeNumber());
        if (InventoryChangeTypeEnum.FREEZE.getType().equals(flow.getChangeType())) {
            return changeNumber;
        }
        if (InventoryChangeTypeEnum.RELEASE.getType().equals(flow.getChangeType())
                || InventoryChangeTypeEnum.CONFIRM.getType().equals(flow.getChangeType())) {
            return -changeNumber;
        }
        return 0;
    }

    /**
     * 修复商品货品表冻结库存。
     *
     * @param productId 商品货品 ID
     * @param expectedLockedStock 期望冻结库存
     * @param actualLockedStock 实际冻结库存
     * @param operator 操作者
     * @return true=修复成功
     */
    private boolean repairLockedStock(Long productId, int expectedLockedStock, int actualLockedStock, String operator) {
        boolean updated = goodsProductService.update(Wrappers.lambdaUpdate(GoodsProduct.class)
                .set(GoodsProduct::getLockedStock, expectedLockedStock)
                .eq(GoodsProduct::getId, productId)
                // 条件更新保证只修复本轮看到的旧值，避免覆盖并发下单或取消刚写入的新冻结库存。
                .eq(GoodsProduct::getLockedStock, actualLockedStock));
        if (updated) {
            log.warn("库存冻结数已自动修复, productId={}, before={}, after={}, operator={}",
                    productId, actualLockedStock, expectedLockedStock, operator);
        }
        return updated;
    }

    /**
     * 构造安全 limit 片段。
     * 定时任务入口可能误传 0、负数或过大的扫描数量，这里统一收敛到单批最大值，避免对商品表造成突发压力。
     *
     * @param limit 原始扫描数量
     * @return MyBatis-Plus last 使用的 limit 片段
     */
    private String limitClause(int limit) {
        int safeLimit = limit <= 0 ? DEFAULT_RECONCILE_LIMIT : Math.min(limit, DEFAULT_RECONCILE_LIMIT);
        return "limit " + safeLimit;
    }

    /**
     * 构建库存对账差异。
     *
     * @param productId 商品货品 ID
     * @param expectedLockedStock 期望冻结库存
     * @param actualLockedStock 实际冻结库存
     * @return 差异明细
     */
    private InventoryReconciliationDifference buildDifference(Long productId, int expectedLockedStock,
                                                              int actualLockedStock) {
        InventoryReconciliationDifference difference = new InventoryReconciliationDifference();
        difference.setProductId(productId);
        difference.setExpectedLockedStock(expectedLockedStock);
        difference.setActualLockedStock(actualLockedStock);
        difference.setMessage(LOCKED_STOCK_MISMATCH_MESSAGE);
        return difference;
    }

    /**
     * 返回非空数量。
     *
     * @param number 数量
     * @return 非空数量
     */
    private int defaultNumber(Integer number) {
        return number == null ? 0 : number;
    }
}
