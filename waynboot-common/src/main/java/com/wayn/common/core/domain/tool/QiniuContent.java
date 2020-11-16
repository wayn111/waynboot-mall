package com.wayn.common.core.domain.tool;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 七牛云文件存储
 * </p>
 *
 * @author wayn
 * @since 2020-11-15
 */
@Data
@TableName("tool_qiniu_content")
@EqualsAndHashCode(callSuper = false)
public class QiniuContent extends ShopBaseEntity implements Serializable {

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
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;


}
