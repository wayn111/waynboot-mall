package com.wayn.common.core.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.ShopBaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("shop_channel")
@EqualsAndHashCode(callSuper = true)
public class Channel extends ShopBaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -7151701194368095457L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "编码不能为空")
    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String remark;
}
