package com.wayn.common.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardRecentVO {
    private List<RecentOrderItem> orders;
    private List<RecentMemberItem> members;

    @Data
    @AllArgsConstructor
    public static class RecentOrderItem {
        private String orderSn;
        private BigDecimal actualPrice;
        private Date createTime;
        private Short orderStatus;
    }

    @Data
    @AllArgsConstructor
    public static class RecentMemberItem {
        private String nickname;
        private String mobile;
        private Date createTime;
    }
}
