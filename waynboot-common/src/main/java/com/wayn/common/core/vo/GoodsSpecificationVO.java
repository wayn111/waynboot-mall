package com.wayn.common.core.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class GoodsSpecificationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3623876658189050505L;

    /**
     * 规格id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品表的商品ID
     */
    private Long goodsId;

    /**
     * 商品规格名称
     */
    private String specification;

    /**
     * 商品规格值
     */
    @NotBlank(message = "商品规格值不能为空")
    private String value;

    /**
     * 商品规格图片
     */
    private String picUrl;

}
