package com.wayn.common.response;

import lombok.Data;

import java.util.Date;

/**
 * 首页栏目配置VO
 */
@Data
public class ColumnManagerResVO {

    private Long id;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 栏目名称
     */
    private String name;

    /**
     * 关联商品数
     */
    private Integer goodsNum;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
