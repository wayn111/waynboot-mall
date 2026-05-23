package com.wayn.common.core.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 订单状态流转日志实体。
 * 用于记录订单状态机每次成功或失败的流转尝试，为支付回调、取消、退款、发货等并发场景提供审计依据。
 */
@Data
@TableName("shop_order_status_log")
@EqualsAndHashCode(callSuper = false)
public class OrderStatusLog extends ShopBaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 5829783085808071462L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单 ID。
     */
    private Long orderId;

    /**
     * 订单号。
     */
    private String orderSn;

    /**
     * 来源状态。
     */
    private Short sourceStatus;

    /**
     * 目标状态。
     */
    private Short targetStatus;

    /**
     * 状态变更类型。
     */
    private String changeType;

    /**
     * 操作者类型。
     */
    private String operatorType;

    /**
     * 操作者 ID 或渠道标识。
     */
    private String operatorId;

    /**
     * 是否流转成功。
     */
    private Boolean success;

    /**
     * 失败原因。
     */
    private String failReason;

    /**
     * 备注。
     */
    private String remark;
}
