package com.wayn.common.request;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class ShopCouponReqVO implements Serializable {

    /**
     * 优惠券名称
     */
    private String title;
    /**
     * 状体 0下架 1上架
     */
    private Integer status;

    /**
     * 类型 1注册赠送 2普通使用
     */
    private Integer type;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
