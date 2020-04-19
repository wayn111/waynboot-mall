package com.wayn.framework.security;

import lombok.Data;

@Data
public class LoginObj {

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 验证码
     */
    private String code;
}
