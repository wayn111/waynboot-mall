package com.wayn.mobile.framework.security;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginObj {

    /**
     * 手机号
     */
    @NotNull
    private String mobile;

    /**
     * 用户密码
     */
    @NotNull
    private String password;

}
