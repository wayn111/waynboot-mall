package com.wayn.common.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author: waynaqua
 * @date: 2024/4/27 16:41
 */
@Data
public class OrderListResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2411154311935053905L;

    /**
     * 我的订单列表
     */
    private List<Map<String, Object>> data;
    /**
     * 总页数
     */
    private Long pages;

    /**
     * 当前页
     */
    private Long page;
}
