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
 * 库存流水实体。
 * 记录订单链路中每一次库存冻结、确认、释放和退款回补，用唯一流水键支撑本地消息重试下的幂等库存变更。
 */
@Data
@TableName("shop_inventory_flow")
@EqualsAndHashCode(callSuper = false)
public class InventoryFlow extends ShopBaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -8454705525587853769L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务唯一流水键。
     */
    private String flowKey;

    /**
     * 业务类型，例如 ORDER。
     */
    private String bizType;

    /**
     * 业务 ID，例如订单号或订单 ID。
     */
    private String bizId;

    /**
     * 商品 ID。
     */
    private Long goodsId;

    /**
     * 商品货品 ID。
     */
    private Long productId;

    /**
     * 库存变更类型。
     */
    private String changeType;

    /**
     * 库存变更数量。
     */
    private Integer changeNumber;

    /**
     * 流水备注。
     */
    private String remark;
}
