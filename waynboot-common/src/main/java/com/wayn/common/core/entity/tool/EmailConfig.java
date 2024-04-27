package com.wayn.common.core.entity.tool;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 邮件配置表
 */
@Data
@TableName("tool_email_config")
public class EmailConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = -8825288678724602467L;

    @TableId(type = IdType.AUTO)
    /**
     * 邮件配置id
     */
    private Long id;

    /**
     * 邮件服务器SMTP地址
     */
    private String host;

    /**
     * 邮件服务器SMTP端口
     */
    private Integer port;

    /**
     * 邮件服务器SMTP SSL端口
     */
    private Integer sslPort;

    /**
     * 邮件服务器授权码
     */
    private String pass;

    /**
     * 发件人邮箱
     */
    private String fromUser;

    /**
     * 发件者用户名
     */
    private String user;
    /**
     * 是否启用 ssl port 0不启用 1启用
     */
    private Integer sslEnable;
    /**
     * 逻辑删除 0存在1删除
     */
    private Integer delFlag;
}
