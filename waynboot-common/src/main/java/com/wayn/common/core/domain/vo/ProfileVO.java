package com.wayn.common.core.domain.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileVO {
    /**
     * 用户昵称或网络名称
     */
    private String nickname;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 用户重复密码
     */
    private String confirmPassword;

    /**
     * 性别：0 未知， 1男， 1 女
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户手机号码
     */
    private String mobile;

}
