package com.wayn.common.util;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderHandleOption {
    /**
     * 取消操作
     */
    private boolean cancel = false;
    /**
     * 删除操作
     */
    private boolean delete = false;
    /**
     * 支付操作
     */
    private boolean pay = false;
    /**
     * 评论操作
     */
    private boolean comment = false;
    /**
     * 确认收货操作
     */
    private boolean confirm = false;
    /**
     * 取消订单并退款操作
     */
    private boolean refund = false;
    /**
     * 再次购买
     */
    private boolean rebuy = false;
    /**
     * 售后操作
     */
    private boolean aftersale = false;

}
