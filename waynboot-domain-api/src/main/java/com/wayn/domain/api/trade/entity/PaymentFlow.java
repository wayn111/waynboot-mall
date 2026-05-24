package com.wayn.domain.api.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.util.entity.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付流水实体。
 * 记录第三方支付回调确认后的渠道流水，用唯一 flowKey 防止同一渠道支付流水重复绑定订单。
 */
@Data
@TableName("shop_payment_flow")
@EqualsAndHashCode(callSuper = false)
public class PaymentFlow extends ShopBaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -2609250972891889518L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 支付流水幂等键，格式为 payChannel:payId。
     */
    private String flowKey;

    /**
     * 订单 ID。
     */
    private Long orderId;

    /**
     * 订单号。
     */
    private String orderSn;

    /**
     * 第三方支付流水号。
     */
    private String payId;

    /**
     * 支付回调渠道编码。
     */
    private String payChannel;

    /**
     * 支付金额。
     */
    private BigDecimal payAmount;

    /**
     * 支付流水状态。
     */
    private Integer status;
}
