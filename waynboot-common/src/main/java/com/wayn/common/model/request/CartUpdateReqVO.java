package com.wayn.common.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 购物车更新请求。
 * 当前仅开放勾选状态修改，数量修改走单独接口。
 */
@Data
public class CartUpdateReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7579396484667756685L;

    /**
     * 购物车 ID
     */
    private Long id;

    /**
     * 是否勾选
     */
    private Boolean checked;
}
