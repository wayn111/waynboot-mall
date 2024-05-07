package com.wayn.mobile.framework.security;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistryObj {


    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String mobile;

    /**
     * 用户密码
     */
    @NotBlank(message = "用户密码不能为空")
    private String password;

    /**
     * 重复密码
     */
    @NotBlank(message = "重复密码不能为空")
    private String confirmPassword;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;

    /**
     * 验证码 key
     */
    @NotBlank
    private String captchaKey;


}
