package com.wayn.common.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardPeriodVO {
    private PeriodItem today;
    private PeriodItem week;
    private PeriodItem month;

    @Data
    @AllArgsConstructor
    public static class PeriodItem {
        private long orderCount;
        private BigDecimal sales;
        private double orderGrowth;
        private double salesGrowth;
    }
}
