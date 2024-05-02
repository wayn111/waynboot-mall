package com.wayn.common.wapper.epay.response;

import lombok.Data;

/**
 * @author: waynaqua
 * @date: 2024/5/1 14:06
 */
@Data
public class EpayResponse {

    /**
     * 返回状态码 1为成功，其它值为失败
     */
    private Integer code;

    /**
     * 返回信息
     */
    private String msg;

    /**
     * 返回是否成功
     *
     * @return
     */
    public Boolean isSuccess() {
        return this.code != null && this.code == 0;
    }
}
