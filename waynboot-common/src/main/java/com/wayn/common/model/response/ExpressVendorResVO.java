package com.wayn.common.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 快递渠道返回。
 */
@Data
public class ExpressVendorResVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5771574429774169886L;

    /**
     * 快递编码
     */
    private String code;

    /**
     * 快递名称
     */
    private String name;
}
