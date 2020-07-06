package com.wayn.admin.api.domain.shop;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wayn.common.base.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 商品规格表
 * </p>
 *
 * @author wayn
 * @since 2020-07-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ShopGoodsSpecification extends ShopBaseEntity implements Serializable {

    private static final long serialVersionUID = 3623876658189050505L;
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品表的商品ID
     */
    private Integer goodsId;

    /**
     * 商品规格名称
     */
    private String specification;

    /**
     * 商品规格值
     */
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
