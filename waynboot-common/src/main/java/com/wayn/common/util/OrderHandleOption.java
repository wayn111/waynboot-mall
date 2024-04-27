package com.wayn.common.util;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderHandleOption {
    private boolean cancel = false;      // 取消操作
    private boolean delete = false;      // 删除操作
    private boolean pay = false;         // 支付操作
    private boolean comment = false;    // 评论操作
    private boolean confirm = false;    // 确认收货操作
    private boolean refund = false;     // 取消订单并退款操作
    private boolean rebuy = false;        // 再次购买
    private boolean aftersale = false;        // 售后操作

}
