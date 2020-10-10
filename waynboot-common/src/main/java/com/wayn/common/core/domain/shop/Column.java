package com.wayn.common.core.domain.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.ShopBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 首页栏目配置
 * </p>
 *
 * @author wayn
 * @since 2020-10-10
 */
@Data
@TableName("shop_column")
@EqualsAndHashCode(callSuper = false)
public class Column extends ShopBaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 栏目名称
     */
    private String name;

}
