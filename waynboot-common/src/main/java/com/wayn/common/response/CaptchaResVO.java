package com.wayn.common.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: waynaqua
 * @date: 2024/4/27 15:34
 */
@Data
public class CaptchaResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2282870703963137881L;

    private String key;

    private String image;
}
