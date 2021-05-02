package com.wayn.common.core.domain.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wayn.common.base.entity.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 评论表
 * </p>
 *
 * @author wayn
 * @since 2020-10-03
 */
@Data
@TableName("shop_comment")
@EqualsAndHashCode(callSuper = false)
public class Comment extends ShopBaseEntity implements Serializable {

    private static final long serialVersionUID = 2415577057902110771L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 如果type=0，则是商品评论；如果是type=1，则是专题评论。
     */
    private Long valueId;

    /**
     * 评论类型，如果type=0，则是商品评论；如果是type=1，则是专题评论；
     */
    private Integer type;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 管理员回复内容
     */
    private String adminContent;

    /**
     * 用户表的用户ID
     */
    private Long userId;

    /**
     * 是否含有图片（0无图，1有图）
     */
    private Boolean hasPicture;

    /**
     * 图片地址列表，采用JSON数组格式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] picUrls;

    /**
     * 评分， 1-5
     */
    private Integer star;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;


}
