package com.wayn.common.core.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送邮件VO对象
 */
@Data
public class SendMailVO implements Serializable {
    private static final long serialVersionUID = 3496419936455305502L;

    /**
     * 发送邮箱
     */
    private String sendMail;
    /**
     * 邮件标题
     */
    private String title;
    /**
     * 邮件内容
     */
    private String content;
}
