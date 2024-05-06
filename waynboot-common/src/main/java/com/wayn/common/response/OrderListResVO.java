package com.wayn.common.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 我的订单接口返回
 */
@Data
public class OrderListResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2411154311935053905L;

    /**
     * 我的订单列表
     */
    private List<OrderListDataResVO> data;
    /**
     * 总页数
     */
    private Long pages;

    /**
     * 当前页
     */
    private Long page;
}
