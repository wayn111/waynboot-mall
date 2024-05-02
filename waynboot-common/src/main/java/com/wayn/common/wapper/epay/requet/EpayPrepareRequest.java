package com.wayn.common.wapper.epay.requet;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: waynaqua
 * @date: 2024/4/30 16:02
 */
@Data
@AllArgsConstructor
public class EpayPrepareRequest {

    /**
     * 商户ID
     */
    private String pid;
    /**
     * 支付方式
     */
    private String type;
    /**
     * 跳转通知地址
     */
    private String notify_url;
    /**
     * 异步通知地址
     */
    private String return_url;
    /**
     * 商户订单号
     */
    private String out_trade_no;
    /**
     * 商品名称
     */
    private String name;
    /**
     * 商品借你
     */
    private String money;
    /**
     * 用户IP地址
     */
    private String clientip;


}
