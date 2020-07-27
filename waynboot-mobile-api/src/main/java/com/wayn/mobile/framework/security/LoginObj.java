package com.wayn.mobile.framework.security;

import lombok.Data;

@Data
public class LoginObj {

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 验证码
     */
    private String code;

    /**
     * 验证码 key
     */
    private String key;
}
