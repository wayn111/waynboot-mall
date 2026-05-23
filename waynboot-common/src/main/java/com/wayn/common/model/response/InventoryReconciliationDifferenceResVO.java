package com.wayn.common.model.response;

import lombok.Data;

/**
 * 库存对账差异响应。
 */
@Data
public class InventoryReconciliationDifferenceResVO {

    /**
     * 商品货品 ID。
     */
    private Long productId;

    /**
     * 流水推导冻结库存。
     */
    private Integer expectedLockedStock;

    /**
     * 商品货品表实际冻结库存。
     */
    private Integer actualLockedStock;

    /**
     * 差异说明。
     */
    private String message;

    /**
     * 是否已修复。
     */
    private Boolean repaired;
}
