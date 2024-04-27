package com.wayn.common.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: waynaqua
 * @date: 2024/4/27 16:44
 */
@Data
public class OrderStatusCountResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1144770796025181947L;

    private Long unpaid;
    private Long unship;
    private Long unrecv;
    private Long uncomment;
}
