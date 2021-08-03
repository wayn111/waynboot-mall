package com.wayn.common.core.domain.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wayn.common.base.entity.ShopBaseEntity;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 商品基本信息表
 * </p>
 *
 * @author wayn
 * @since 2020-07-06
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName(value = "shop_goods", autoResultMap = true)
public class Goods extends ShopBaseEntity implements Serializable {

    private static final long serialVersionUID = 3591926398371563001L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品编号
     */
    private String goodsSn;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名不能为空")
    private String name;

    /**
     * 商品所属类目ID
     */
    @Min(value = 0, message = "商品所属类目不能为空")
    private Long categoryId;

    private Long brandId;

    /**
     * 商品宣传图片列表，采用JSON数组格式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] gallery;

    /**
     * 商品关键字，采用逗号间隔
     */
    private String keywords;

    /**
     * 商品简介
     */
    private String brief;

    /**
     * 是否上架（0代表为上架 1代表上架）
     */
    private Boolean isOnSale;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 商品页面商品图片
     */
    private String picUrl;

    /**
     * 商品分享海报
     */
    private String shareUrl;

    /**
     * 是否新品首发，如果设置则可以在新品首发页面展示
     */
    private Boolean isNew;

    /**
     * 是否人气推荐，如果设置则可以在人气推荐页面展示
     */
    private Boolean isHot;

    /**
     * 商品单位，例如件、盒
     */
    @NotBlank(message = "商品单位不能为空")
    private String unit;

    /**
     * 专柜价格
     */
    private BigDecimal counterPrice;

    /**
     * 零售价格
     */
    private BigDecimal retailPrice;

    /**
     * 实际销量
     */
    private Integer actualSales;

    /**
     * 虚拟销量
     */
    private Integer virtualSales;

    /**
     * 商品详细介绍，是富文本格式
     */
    private String detail;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;


}
