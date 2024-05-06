package com.wayn.common.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 我的订单列表状态统计
 */
@Data
public class OrderStatusCountResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1144770796025181947L;

    /**
     * 未支付订单数量
     */
    private Long unpaid;
    /**
     * 未发货订单数量
     */
    private Long unship;
    /**
     * 未收获订单数量
     */
    private Long unrecv;
    /**
     * 未评价订单数量
     */
    private Long uncomment;
}
