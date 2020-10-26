package com.wayn.mobile.framework.security;

import lombok.Data;

@Data
public class RegistryObj {

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 重复密码
     */
    private String confirmPassword;

    /**
     * 验证码
     */
    private String code;

    /**
     * 邮箱验证码
     */
    private String emailCode;

    /**
     * 验证码 key
     */
    private String key;

    /**
     * 邮箱验证码 key
     */
    private String emailKey;

}
