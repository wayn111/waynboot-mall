package com.wayn.common.core.service.trade;

import lombok.Data;

/**
 * 交易表路由结果。
 * 描述订单主表、订单明细、支付流水和本地消息表在同一月份分片下的物理表名。
 */
@Data
public class TradeTableRoute {

    /**
     * 订单主表物理表名。
     */
    private String orderTable;

    /**
     * 订单明细表物理表名。
     */
    private String orderGoodsTable;

    /**
     * 支付流水表物理表名。
     */
    private String paymentFlowTable;

    /**
     * 本地消息表物理表名。
     */
    private String localMessageTable;
}
