package com.wayn.common.core.domain.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 邮件配置表
 */
@Data
public class EmailConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8825288678724602467L;

    private Long id;

    /**
     * 邮件服务器SMTP地址
     */
    @NotBlank(message = "邮件服务器SMTP地址不能为空")
    private String host;

    /**
     * 邮件服务器SMTP端口
     */
    @NotBlank(message = "邮件服务器SMTP端口不能为空")
    private Integer port;

    private Integer sslPort;

    @NotBlank(message = "邮箱密钥不能为空")
    private String pass;

    /**
     * 发件人邮箱
     */
    @NotBlank(message = "发件人邮箱不能为空")
    private String fromUser;

    /**
     * 发件者用户名
     */
    private String user;

    /**
     * 逻辑删除 0存在1删除
     */
    private Integer delFlag;
}
