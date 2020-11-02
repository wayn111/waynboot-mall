package com.wayn.common.core.domain.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 关键字表
 * </p>
 *
 * @author wayn
 * @since 2020-11-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Keyword implements Serializable {

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
    private Integer sortOrder;

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
