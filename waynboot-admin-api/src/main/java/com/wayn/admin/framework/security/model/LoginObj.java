package com.wayn.admin.framework.security.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginObj {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 用户密码
     */
    @NotBlank(message = "用户密码不能为空")
    private String password;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
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
