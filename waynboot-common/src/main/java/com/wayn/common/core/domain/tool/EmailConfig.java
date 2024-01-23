package com.wayn.common.core.domain.tool;

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
    private Long id;

    /**
     * 邮件服务器SMTP地址
     */
    private String host;

    /**
     * 邮件服务器SMTP端口
     */
    private Integer port;

    private Integer sslPort;

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
