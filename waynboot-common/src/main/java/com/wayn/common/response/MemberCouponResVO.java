package com.wayn.common.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class MemberCouponResVO implements Serializable {
    /**
     * id
     */
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
     * 创建时间
     */
    private Date createTime;
    /**
     * 过期时间
     */
    private Date expireTime;
    @Serial
    private static final long serialVersionUID = 1L;
}
