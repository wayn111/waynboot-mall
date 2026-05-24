package com.wayn.domain.api.promotion.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 后台优惠券管理返回。
 */
@Data
public class ShopCouponManageResVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2796536097589055481L;

    private Integer id;
    private String title;
    private Integer num;
    private Integer receiveNum;
    private Integer discount;
    private Integer min;
    private Integer status;
    private Integer type;
    private Date expireTime;
    private Date createTime;
    private String createBy;
    private Date updateTime;
    private String updateBy;
    private Integer delFlag;
}
