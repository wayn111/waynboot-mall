package com.wayn.common.core.domain.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 七牛云配置
 */
@Data
public class QiniuConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * accessKey
     */
    @NotBlank(message = "accessKey不能为空")
    private String accessKey;

    /**
     * Bucket 识别符
     */
    @NotBlank(message = "Bucket不能为空")
    private String bucket;

    /**
     * 外链域名
     */
    @NotBlank(message = "外链域名不能为空")
    private String host;

    /**
     * secretKey
     */
    @NotBlank(message = "secretKey不能为空")
    private String secretKey;

    /**
     * 空间类型 0 公开 1 私有
     */
    private Integer type;

    /**
     * 存储区域
     */
    private String region;

    /**
     * 是否启用七牛云存储 0 禁用 1 启用
     */
    private Boolean enable;

    /**
     * 逻辑删除 0存在1删除
     */
    private Integer delFlag;
}
