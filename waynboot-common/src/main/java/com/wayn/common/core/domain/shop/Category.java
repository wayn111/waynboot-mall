package com.wayn.common.core.domain.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 类目表
 * </p>
 *
 * @author wayn
 * @since 2020-06-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("shop_category")
public class Category extends ShopBaseEntity implements Serializable {

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

    /**
     * 类目级别 L1 一级 L2 二级
     */
    private String level;

    /**
     * 排序
     */
    private Integer sort;


    /**
     * banner状态（0启用 1禁用）
     */
    private Boolean delFlag;

    /**
     * 备注
     */
    private String remark;
}
