package com.wayn.common.core.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 发送邮件VO对象
 */
@Data
public class SendMailVO implements Serializable {
    private static final long serialVersionUID = 3496419936455305502L;

    /**
     * 收件人，支持多个收件人
     */
    @NotEmpty
    private List<String> tos;

    @NotBlank
    private String subject;

    @NotBlank
    private String content;
}
