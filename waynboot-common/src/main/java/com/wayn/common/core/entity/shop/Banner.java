package com.wayn.common.core.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.ShopBaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * banner图片表
 */
@Data
@TableName("shop_banner")
@ToString
@EqualsAndHashCode(callSuper = true)
public class Banner extends ShopBaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -6208877431925919530L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "banner图片不能为空")
    private String imgUrl;

    /**
     * 条跳转链接
     */
    private String jumpUrl;

    /**
     * 排序默认从小到大
     */
    private Integer sort;

    /**
     * banner状态（0启用 1禁用）
     */
    private Integer status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;
}
