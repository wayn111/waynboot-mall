package com.wayn.common.core.domain.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 订单商品表
 * </p>
 *
 * @author wayn
 * @since 2020-08-11
 */
@Data
@TableName(value = "shop_order_goods", autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
public class OrderGoods implements Serializable {

    private static final long serialVersionUID = -8548938788899621492L;

    @TableId(type = IdType.AUTO)
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
     * 商品货品表的货品ID
     */
    private Long productId;

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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] specifications;

    /**
     * 商品货品图片或者商品图片
     */
    private String picUrl;

    /**
     * 订单商品评论，如果是-1，则超期不能评价；如果是0，则可以评价；如果其他值，则是comment表里面的评论ID。
     */
    private Integer comment;

    /**
     * 创建时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;


}
