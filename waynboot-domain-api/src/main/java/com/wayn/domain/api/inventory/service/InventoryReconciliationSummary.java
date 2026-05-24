package com.wayn.domain.api.inventory.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 库存对账汇总。
 * 汇总本次扫描 SKU 数量、冻结库存不一致数量以及自动修复结果，供定时任务和运营后台使用。
 */
@Data
public class InventoryReconciliationSummary {

    /**
     * 扫描 SKU 数量。
     */
    private int checkedProductCount;

    /**
     * 冻结库存不一致数量。
     */
    private int mismatchCount;

    /**
     * 已自动修复数量。
     */
    private int repairedCount;

    /**
     * 对账差异列表。
     */
    private List<InventoryReconciliationDifference> differences = new ArrayList<>();

    /**
     * 添加冻结库存差异。
     *
     * @param difference 差异明细
     */
    public void addDifference(InventoryReconciliationDifference difference) {
        mismatchCount++;
        differences.add(difference);
    }

    /**
     * 增加修复计数。
     */
    public void incrementRepairedCount() {
        repairedCount++;
    }
}
