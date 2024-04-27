package com.wayn.common.core.entity.tool;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 七牛云配置
 */
@Data
@TableName("tool_qiniu_config")
public class QiniuConfig implements Serializable {

    @Serial
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
     * 是否启用七牛云存储 0 禁用 1 启用
     */
    private Boolean enable;

    /**
     * 逻辑删除 0存在1删除
     */
    private Integer delFlag;
}
