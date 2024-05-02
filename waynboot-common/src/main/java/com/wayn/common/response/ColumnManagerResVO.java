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

    private Date createTime;

    private Date updateTime;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;

}
