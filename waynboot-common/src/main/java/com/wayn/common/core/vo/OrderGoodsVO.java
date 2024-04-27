package com.wayn.common.core.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单商品表
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrderGoodsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8548938788899621492L;
    private Long id;

    /**
     * 订单表的订单ID
     */
    private Long orderId;

    /**
     * 商品表的商品ID
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品编号
     */
    private String goodsSn;

    /**
     * 商品货品的购买数量
     */
    private Integer number;

    /**
     * 商品货品的售价
     */
    private BigDecimal price;

    /**
     * 商品货品的规格列表
     */
    private String[] specifications;

    /**
     * 商品货品图片或者商品图片
     */
    private String picUrl;

    /**
     * 订单商品评论，如果是-1，则超期不能评价；如果是0，则可以评价；如果其他值，则是comment表里面的评论ID。
     */
    private Integer comment;

}
