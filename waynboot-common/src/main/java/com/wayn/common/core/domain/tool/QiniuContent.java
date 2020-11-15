package com.wayn.common.core.domain.tool;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 七牛云文件存储
 * </p>
 *
 * @author wayn
 * @since 2020-11-15
 */
@Data
@TableName("qiniu_content")
@EqualsAndHashCode(callSuper = false)
public class QiniuContent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "content_id", type = IdType.AUTO)
    private Long contentId;

    /**
     * Bucket 识别符
     */
    private String bucket;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件大小
     */
    private String size;

    /**
     * 文件类型：私有或公开
     */
    private String type;

    /**
     * 文件url
     */
    private String url;

    /**
     * 文件后缀
     */
    private String suffix;

    /**
     * 上传或同步的时间
     */
    private LocalDateTime updateTime;


}
