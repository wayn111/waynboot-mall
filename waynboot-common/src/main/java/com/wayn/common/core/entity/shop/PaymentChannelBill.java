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
import java.util.Date;

/**
 * 支付渠道账单实体。
 * 保存第三方渠道账单明细，用于日终对账时和内部支付流水、订单实付金额做差异识别。
 */
@Data
@TableName("shop_payment_channel_bill")
@EqualsAndHashCode(callSuper = false)
public class PaymentChannelBill extends ShopBaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -789452208467430601L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 渠道账单日期。
     */
    private Date billDate;

    /**
     * 订单号。
     */
    private String orderSn;

    /**
     * 第三方支付流水号。
     */
    private String payId;

    /**
     * 支付渠道编码。
     */
    private String payChannel;

    /**
     * 渠道账单支付金额。
     */
    private BigDecimal payAmount;

    /**
     * 账单状态。
     */
    private String billStatus;
}
