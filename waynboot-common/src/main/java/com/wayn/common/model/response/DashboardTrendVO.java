package com.wayn.common.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardTrendVO {
    private List<String> dates;
    private List<Long> orderCounts;
    private List<BigDecimal> sales;
}
