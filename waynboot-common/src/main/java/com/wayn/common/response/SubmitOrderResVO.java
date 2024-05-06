package com.wayn.common.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提交订单接口返回
 */
@Data
public class SubmitOrderResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5315035497859332626L;

    /**
     * 订单实际支付价格
     */
    private BigDecimal actualPrice;

    /**
     * 订单编号
     */
    private String orderSn;
}
