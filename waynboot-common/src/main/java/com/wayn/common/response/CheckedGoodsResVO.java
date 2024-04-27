package com.wayn.common.response;

import com.wayn.common.core.entity.shop.Cart;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author: waynaqua
 * @date: 2024/4/27 16:06
 */
@Data
public class CheckedGoodsResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -7439295637660336892L;

    private List<Cart> data;
    private BigDecimal freightPrice;
    private BigDecimal goodsAmount;
    private BigDecimal orderTotalAmount;
}
