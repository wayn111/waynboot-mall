package com.wayn.common.model.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardStatsVO {
    private Long memberCount;
    private Long todayMemberCount;
    private Long todayOrderCount;
    private Long totalOrderCount;
    private BigDecimal totalSales;
    private BigDecimal todaySales;
    private Long onSaleGoodsCount;
    private Long lowStockCount;
    // 订单状态分组
    private Long pendingPayCount;       // 待付款 101
    private Long pendingShipCount;      // 待发货 201
    private Long pendingReceiveCount;   // 待收货 301
    private Long completedOrderCount;   // 已完成 401+402
    private Long closedOrderCount;      // 已关闭 102+103
    // 退款申请
    private Long refundCount;           // 申请退款 202
    // 支付转化率（今日已支付/今日下单）
    private BigDecimal conversionRate;
}
