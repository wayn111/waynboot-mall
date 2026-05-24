package com.wayn.domain.api.inventory.service;

import lombok.Data;

/**
 * 库存对账差异。
 * 用于记录库存流水推导出的冻结库存和商品货品表实际冻结库存之间的差异。
 */
@Data
public class InventoryReconciliationDifference {

    /**
     * 商品货品 ID。
     */
    private Long productId;

    /**
     * 库存流水推导出的冻结库存。
     */
    private Integer expectedLockedStock;

    /**
     * 商品货品表当前冻结库存。
     */
    private Integer actualLockedStock;

    /**
     * 差异说明。
     */
    private String message;

    /**
     * 是否已经自动修复。
     */
    private boolean repaired;
}
