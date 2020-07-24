package com.wayn.common.core.model;

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
}
