package com.wayn.common.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: waynaqua
 * @date: 2024/6/6 13:58
 */
@Data
public class ShopCouponGiveUserReqVO implements Serializable {

    /**
     * 用户id
     */
    @NotNull
    private Integer userId;

    /**
     * 优惠卷id
     */
    @NotNull
    private Integer couponId;

}
