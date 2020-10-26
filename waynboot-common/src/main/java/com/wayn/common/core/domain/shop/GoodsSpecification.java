package com.wayn.common.core.domain.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * <p>
 * 商品规格表
 * </p>
 *
 * @author wayn
 * @since 2020-07-06
 */
@Data
@TableName("shop_goods_specification")
@EqualsAndHashCode(callSuper = false)
public class GoodsSpecification extends ShopBaseEntity implements Serializable {

    private static final long serialVersionUID = 3623876658189050505L;

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

    /**
     * 逻辑删除
     */
    private Boolean delFlag;


}
