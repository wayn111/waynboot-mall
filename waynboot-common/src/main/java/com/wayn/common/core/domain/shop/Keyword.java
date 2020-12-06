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
 * 关键字表
 * </p>
 *
 * @author wayn
 * @since 2020-11-02
 */
@Data
@TableName("shop_keyword")
@EqualsAndHashCode(callSuper = false)
public class Keyword extends ShopBaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 关键字跳转类型（0关键字跳转，1url跳转）
     */
    private Boolean jumpType;

    /**
     * 关键字的跳转链接
     */
    private String url;

    /**
     * 是否是热门关键字
     */
    private Boolean isHot;

    /**
     * 是否是默认关键字
     */
    private Boolean isDefault;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;


}
