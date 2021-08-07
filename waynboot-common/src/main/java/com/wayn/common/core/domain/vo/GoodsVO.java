package com.wayn.common.core.domain.vo;

import com.wayn.common.base.entity.ShopBaseEntity;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

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
public class GoodsVO extends ShopBaseEntity implements Serializable {

    private static final long serialVersionUID = 3591926398371563001L;

    private Long id;

    /**
     * 商品编号
     */
    private String goodsSn;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品所属类目ID
     */
    private Long categoryId;

    private Long brandId;

    /**
     * 商品宣传图片列表，采用JSON数组格式
     */
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

    private List<Long> goodsIdList;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;


}
