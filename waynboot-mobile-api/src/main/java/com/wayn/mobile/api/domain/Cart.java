package com.wayn.mobile.api.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 购物车商品表
 * </p>
 *
 * @author wayn
 * @since 2020-08-03
 */
@Data
@TableName(value = "shop_cart", autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户表的用户ID
     */
    private Integer userId;

    /**
     * 商品表的商品ID
     */
    private Integer goodsId;

    /**
     * 商品编号
     */
    private String goodsSn;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品货品表的货品ID
     */
    private Integer productId;

    /**
     * 商品货品的价格
     */
    private BigDecimal price;

    /**
     * 商品货品的数量
     */
    private Integer number;

    /**
     * 商品规格值列表，采用JSON数组格式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] specifications;

    /**
     * 购物车中商品是否选择状态
     */
    private Boolean checked;

    /**
     * 商品图片或者商品货品图片
     */
    private String picUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;


}
