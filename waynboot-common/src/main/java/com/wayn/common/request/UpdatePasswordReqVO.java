package com.wayn.common.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: waynaqua
 * @date: 2024/4/27 19:46
 */
@Data
public class UpdatePasswordReqVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -8567410355614947736L;

    /**
     * 旧密码
     */
    @NotBlank
    private String oldPassword;
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

}
