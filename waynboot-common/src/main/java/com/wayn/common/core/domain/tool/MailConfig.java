package com.wayn.common.core.domain.tool;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@TableName("tool_email_config")
public class MailConfig implements Serializable {

    private static final long serialVersionUID = -8825288678724602467L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 邮件服务器SMTP地址 */
    @NotBlank
    private String host;

    /** 邮件服务器SMTP端口 */
    @NotBlank
    private String port;

    @NotBlank
    private String pass;

    /** 发件者用户名，默认为发件人邮箱前缀 */
    @NotBlank
    private String fromUser;
}
