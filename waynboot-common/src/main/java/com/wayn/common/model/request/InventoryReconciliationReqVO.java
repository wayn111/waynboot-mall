package com.wayn.common.model.request;

import lombok.Data;

import java.util.List;

/**
 * 库存流水对账请求。
 * 支持指定 SKU 对账，也支持按最近更新 SKU 批量扫描。
 */
@Data
public class InventoryReconciliationReqVO {

    /**
     * 指定对账的商品货品 ID。
     */
    private List<Long> productIds;

    /**
     * 最近更新 SKU 扫描数量。
     */
    private Integer limit;

    /**
     * 是否自动修复 locked_stock。
     */
    private Boolean repair;
}
