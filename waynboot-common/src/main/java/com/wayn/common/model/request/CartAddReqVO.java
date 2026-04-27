package com.wayn.common.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 购物车新增请求。
 * 同时兼容普通加购和按默认货品加购场景，默认货品场景下 `productId` 可为空。
 */
@Data
public class CartAddReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3657175686976976308L;

    /**
     * 商品 ID
     */
    private Long goodsId;

    /**
     * 货品 ID
     */
    private Long productId;

    /**
     * 加购数量
     */
    private Integer number;
}
