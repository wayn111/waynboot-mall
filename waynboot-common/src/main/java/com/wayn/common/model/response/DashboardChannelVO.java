package com.wayn.common.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DashboardChannelVO {
    private String channelName;
    private long orderCount;
    private BigDecimal sales;
}
