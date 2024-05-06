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

    /**
     * 验证码key，用于校验使用
     */
    private String key;

    /**
     * 验证码图片转base64
     */
    private String image;
}
