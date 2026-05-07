package com.wayn.common.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量下单回调结果。
 * 用于消费端判断整批消息是否全部落单成功，同时保留每个订单的成功或失败明细。
 */
@Data
public class BatchOrderSubmitResVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7820767770786694048L;

    /**
     * 已成功落单或已幂等跳过的订单编号列表。
     */
    private List<String> successOrderSnList = new ArrayList<>();

    /**
     * 落单失败的订单编号和失败原因。
     */
    private Map<String, String> failedOrderSnMap = new LinkedHashMap<>();
}
