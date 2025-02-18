package com.wayn.common.core.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 优惠券用户使用表
 *
 * @TableName shop_member_coupon
 */
@TableName(value = "shop_member_coupon")
@Data
public class ShopMemberCoupon implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 优惠券
     */
    private String title;

    /**
     * 优惠券ID
     */
    private Integer couponId;

    /**
     * 优惠金额
     */
    private Integer discount;

    /**
     * 使用门槛金额
     */
    private Integer min;

    /**
     * 使用状态 0未使用 1已使用 2已过期
     */
    private Integer useStatus;

    /**
     * 使用时间
     */
    private Date usedTime;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 逻辑删除 0存在 1删除
     */
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
