package com.wayn.common.core.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付退款流水实体。
 * 记录退款成功或失败的渠道结果，用于和订单退款状态、退款金额进行日终对账。
 */
@Data
@TableName("shop_payment_refund_flow")
@EqualsAndHashCode(callSuper = false)
public class PaymentRefundFlow extends ShopBaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -6258904393460759014L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 退款流水幂等键。
     */
    private String refundKey;

    /**
     * 订单 ID。
     */
    private Long orderId;

    /**
     * 订单号。
     */
    private String orderSn;

    /**
     * 第三方退款流水号。
     */
    private String refundId;

    /**
     * 退款渠道编码。
     */
    private String refundChannel;

    /**
     * 退款金额。
     */
    private BigDecimal refundAmount;

    /**
     * 退款状态。
     */
    private Integer status;
}
