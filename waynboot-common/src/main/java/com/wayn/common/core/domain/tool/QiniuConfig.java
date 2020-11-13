package com.wayn.common.core.domain.tool;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 七牛云配置
 * </p>
 *
 * @author wayn
 * @since 2020-11-13
 */
@Data
@TableName("tool_email_config")
public class QiniuConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * accessKey
     */
    private String accessKey;

    /**
     * Bucket 识别符
     */
    private String bucket;

    /**
     * 外链域名
     */
    private String host;

    /**
     * secretKey
     */
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
     * 是否启用七牛云存储 0 启用 1 禁用
     */
    private Integer enable;


}
