package com.wayn.mobile.framework.security;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginObj {

    /**
     * 手机号
     */
    @NotNull(message = "手机号不能为空")
    private String mobile;

    /**
     * 验证码
     */
    @NotNull(message = "验证码不能为空")
    private String yzm;

}
