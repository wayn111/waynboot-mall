package com.wayn.common.model.response;

import lombok.Data;

import java.util.List;

/**
 * 库存对账汇总响应。
 */
@Data
public class InventoryReconciliationSummaryResVO {

    /**
     * 扫描 SKU 数量。
     */
    private Integer checkedProductCount;

    /**
     * 差异数量。
     */
    private Integer mismatchCount;

    /**
     * 自动修复数量。
     */
    private Integer repairedCount;

    /**
     * 差异明细。
     */
    private List<InventoryReconciliationDifferenceResVO> differences;
}
