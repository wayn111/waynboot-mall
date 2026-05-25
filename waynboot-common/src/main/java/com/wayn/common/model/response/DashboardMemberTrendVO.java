package com.wayn.common.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardMemberTrendVO {
    private List<String> dates;
    private List<Long> counts;
}
