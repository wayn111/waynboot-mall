package com.wayn.admin.api.domain.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wayn.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShopBanner extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -6208877431925919530L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String imgUrl;

    private String page;

    private String jumpUrl;

    private Integer sort;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private Integer delFlag;
}
