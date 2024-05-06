package com.wayn.common.response;

import com.wayn.common.core.entity.shop.Cart;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 统计购物车中已勾选商品数量
 */
@Data
public class CheckedGoodsResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -7439295637660336892L;

    /**
     * 购物车列表
     */
    private List<Cart> data;

    /**
     * 运费
     */
    private BigDecimal freightPrice;

    /**
     * 购物车已勾选商品总价
     */
    private BigDecimal goodsAmount;

    /**
     * 购物车已勾选商品总价加运费
     */
    private BigDecimal orderTotalAmount;
}
