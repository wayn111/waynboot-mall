package com.wayn.admin.framework.security.model;

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

    /**
     * 验证码 key
     */
    private String key;


    /**
     * 滑块验证码，后台二次校验参数
     */
    private String captchaVerification;
}
