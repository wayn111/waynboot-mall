package com.wayn.common.core.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 优惠券
 *
 * @TableName shop_coupon
 */
@TableName(value = "shop_coupon")
@Data
public class ShopCoupon implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 优惠券名称
     */
    private String title;

    /**
     * 发放数量
     */
    private Integer num;

    /**
     * 领取数量
     */
    private Integer receiveNum;

    /**
     * 优惠金额
     */
    private Integer discount;

    /**
     * 使用门槛金额
     */
    private Integer min;

    /**
     * 状体 0下架 1上架
     */
    private Integer status;

    /**
     * 类型 1注册赠送 2普通使用
     */
    private Integer type;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 逻辑删除 0存在 1删除
     */
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
