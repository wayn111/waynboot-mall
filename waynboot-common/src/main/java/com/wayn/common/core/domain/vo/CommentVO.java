package com.wayn.common.core.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class CommentVO {

    private Long id;

    /**
     * 订单商品项ID
     */
    private Long orderGoodsId;

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
     * 评论用户头像
     */
    private String avatar;

    /**
     * 评论用户名称
     */
    private String username;

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date createTime;
}
