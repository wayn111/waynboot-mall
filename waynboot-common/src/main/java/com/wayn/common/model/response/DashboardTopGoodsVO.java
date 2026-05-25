package com.wayn.common.model.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardTopGoodsVO {
    private Long goodsId;
    private String name;
    private String picUrl;
    private Integer actualSales;
    private BigDecimal retailPrice;
    private Integer stock;  // 当前可售库存（number 字段）
    private String sku;     // SKU 编码（goodsSn）
}

