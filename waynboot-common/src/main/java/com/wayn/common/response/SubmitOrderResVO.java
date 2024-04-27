package com.wayn.common.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author: waynaqua
 * @date: 2024/4/27 16:46
 */
@Data
public class SubmitOrderResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5315035497859332626L;

    private BigDecimal actualPrice;
    private String orderSn;
}
