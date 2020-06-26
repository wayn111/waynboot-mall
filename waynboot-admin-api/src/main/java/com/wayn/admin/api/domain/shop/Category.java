package com.wayn.admin.api.domain.shop;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

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
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private Integer pid;

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
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * banner状态（0启用 1禁用）
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private String createBy;

    /**
     * 创建人
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;


}
