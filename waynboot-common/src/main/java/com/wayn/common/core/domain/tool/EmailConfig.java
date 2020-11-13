package com.wayn.common.core.domain.tool;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@TableName("tool_email_config")
public class EmailConfig implements Serializable {

    private static final long serialVersionUID = -8825288678724602467L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 邮件服务器SMTP地址 */
    @NotBlank(message = "邮件服务器SMTP地址不能为空")
    private String host;

    /** 邮件服务器SMTP端口 */
    @NotBlank(message = "邮件服务器SMTP端口不能为空")
    private String port;

    @NotBlank(message = "邮箱密钥不能为空")
    private String pass;

    /** 发件人邮箱 */
    @NotBlank(message = "发件人邮箱不能为空")
    private String fromUser;

    /** 发件者用户名 */
    private String user;
}
