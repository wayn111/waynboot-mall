package com.wayn.mobile.api.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 搜索历史表
 * </p>
 *
 * @author wayn
 * @since 2020-09-23
 */
@Data
@TableName("shop_search_history")
@EqualsAndHashCode(callSuper = false)
public class SearchHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户表的用户ID
     */
    private Long userId;

    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 搜索来源，如pc、wx、app
     */
    private String from;

    /**
     * 搜索记录是否有商品结果（0没有 1有）
     */
    private Boolean hasGoods;

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
