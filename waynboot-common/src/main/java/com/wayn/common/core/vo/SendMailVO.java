package com.wayn.common.core.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 发送邮件VO对象
 */
@Data
public class SendMailVO implements Serializable {
    @Serial
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
