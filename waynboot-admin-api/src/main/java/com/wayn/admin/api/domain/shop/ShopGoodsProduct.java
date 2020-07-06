package com.wayn.admin.api.domain.shop;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wayn.common.base.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 商品货品表
 * </p>
 *
 * @author wayn
 * @since 2020-07-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ShopGoodsProduct extends ShopBaseEntity implements Serializable {

    private static final long serialVersionUID = 1472445142327045417L;
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品表的商品ID
     */
    private Integer goodsId;

    /**
     * 商品规格值列表，采用JSON数组格式
     */
    private String specifications;

    /**
     * 商品货品价格
     */
    private BigDecimal price;

    /**
     * 商品货品数量
     */
    private Integer number;

    /**
     * 商品货品图片
     */
    private String url;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;


}
