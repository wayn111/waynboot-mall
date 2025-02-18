package com.wayn.common.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: waynaqua
 * @date: 2024/6/13 15:51
 */
@Data
public class CouponReceiveReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 优惠卷id
     */
    @NotNull
    private Integer couponId;
}
