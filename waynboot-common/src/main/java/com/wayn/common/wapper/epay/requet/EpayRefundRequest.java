package com.wayn.common.wapper.epay.requet;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: waynaqua
 * @date: 2024/4/30 16:02
 */
@Data
@AllArgsConstructor
public class EpayRefundRequest {

    /**
     * 商户ID
     */
    private String pid;
    /**
     * 商户密钥
     */
    private String key;

    /**
     * 商户订单号
     */
    private String out_trade_no;

    /**
     * 退款金额
     */
    private String money;


}
