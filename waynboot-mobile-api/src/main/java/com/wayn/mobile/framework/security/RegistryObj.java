package com.wayn.mobile.framework.security;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistryObj {

    /**
     * 用户名
     */
    @NotBlank
    private String username;

    /**
     * 手机号
     */
    @NotBlank
    private String mobile;

    /**
     * 用户密码
     */
    @NotBlank
    private String password;

    /**
     * 重复密码
     */
    @NotBlank
    private String confirmPassword;

    /**
     * 验证码
     */
    @NotBlank
    private String captchaCode;

    /**
     * 验证码 key
     */
    @NotBlank
    private String captchaKey;


}
