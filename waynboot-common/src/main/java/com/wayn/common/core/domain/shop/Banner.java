package com.wayn.common.core.domain.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@TableName("shop_banner")
@ToString
@EqualsAndHashCode(callSuper = true)
public class Banner extends ShopBaseEntity implements Serializable {

    private static final long serialVersionUID = -6208877431925919530L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "banner图片不能为空")
    private String imgUrl;

    private String jumpUrl;

    private Integer sort;

    private Integer status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;
}
