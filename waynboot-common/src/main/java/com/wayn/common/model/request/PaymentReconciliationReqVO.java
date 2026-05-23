package com.wayn.common.model.request;

import lombok.Data;

import java.util.Date;

/**
 * 支付对账查询请求 VO。
 * 管理端可按时间窗口限制单次对账扫描范围，避免一次性扫描过多历史数据。
 */
@Data
public class PaymentReconciliationReqVO {

    /**
     * 开始时间。
     */
    private Date startTime;

    /**
     * 结束时间。
     */
    private Date endTime;

    /**
     * 单次扫描数量。
     */
    private Integer limit;
}
