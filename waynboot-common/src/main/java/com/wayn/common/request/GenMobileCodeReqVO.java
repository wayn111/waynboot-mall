package com.wayn.common.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: waynaqua
 * @date: 2024/5/3 14:12
 */
@Data
public class GenMobileCodeReqVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5297627773353007514L;

    @NotBlank(message = "手机号不能为空")
    private String mobile;
}
