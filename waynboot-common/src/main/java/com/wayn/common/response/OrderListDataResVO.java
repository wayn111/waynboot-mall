package com.wayn.common.response;

import com.wayn.common.core.vo.OrderGoodsVO;
import com.wayn.common.util.OrderHandleOption;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author: waynaqua
 * @date: 2024/5/6 15:10
 */
@Data
public class OrderListDataResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long id;
    /**
     * 订单编号
     */
    private String orderSn;
    /**
     * 实际支付金额
     */
    private BigDecimal actualPrice;
    /**
     * 订单状态
     */
    private String orderStatusText;

    /**
     * 订单操作
     */
    private OrderHandleOption handleOption;

    /**
     * 订单关联商品列表
     */
    private List<OrderGoodsVO> goodsList;
}
