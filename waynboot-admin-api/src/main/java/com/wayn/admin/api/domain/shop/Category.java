package com.wayn.admin.api.domain.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 类目表
 * </p>
 *
 * @author jobob
 * @since 2020-06-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("shop_category")
public class Category extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 类目名称
     */
    private String name;

    /**
     * 类目关键字，以JSON数组格式
     */
    private String keywords;

    /**
     * 类目广告语介绍
     */
    private String desc;

    /**
     * 父类目ID
     */
    private Long pid;

    /**
     * 类目图标
     */
    private String iconUrl;

    /**
     * 类目图片
     */
    private String picUrl;

    private String level;

    /**
     * 排序
     */
    private Integer sortOrder;


    /**
     * banner状态（0启用 1禁用）
     */
    private Integer delFlag;
}
