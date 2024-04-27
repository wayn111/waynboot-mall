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

    private List<Map<String, Object>> data;
    private Long pages;
    private Long page;
}
