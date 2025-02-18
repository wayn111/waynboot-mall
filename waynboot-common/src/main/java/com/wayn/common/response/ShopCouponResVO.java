package com.wayn.common.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class ShopCouponResVO implements Serializable {
    /**
     * 主键
     */
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
     * 领取状态 0未领取 1已领取
     */
    private Integer receiveStatus;

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

    @Serial
    private static final long serialVersionUID = 1L;
}
