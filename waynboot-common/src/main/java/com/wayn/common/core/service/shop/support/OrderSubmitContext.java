package com.wayn.common.core.service.shop.support;

import com.wayn.common.core.entity.shop.Address;
import com.wayn.common.core.entity.shop.Cart;

import java.math.BigDecimal;
import java.util.List;

/**
 * 下单上下文。
 * 收敛下单金额计算和订单落库前需要的聚合数据，便于编排层和组装层之间传递。
 */
public record OrderSubmitContext(Address address,
                                 List<Cart> checkedGoodsList,
                                 BigDecimal checkedGoodsPrice,
                                 BigDecimal freightPrice,
                                 BigDecimal orderTotalPrice,
                                 BigDecimal couponPrice,
                                 BigDecimal actualPrice) {
}
