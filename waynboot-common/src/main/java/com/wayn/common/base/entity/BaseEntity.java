package com.wayn.common.base.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础entity类，集成以获取通用字段
 */
@Data
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1956057929467119856L;
    /**
     * 创建者
     */
    private String createBy;
    /**
     * 创建时间
     */
    @Excel(name = "创建时间", format="yyyy-MM-dd HH:mm:ss" ,width = 25)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 备注
     */
    @Excel(name = "备注", width = 25)
    private String remark;

    /**
     * 开始时间
     */
    @JsonIgnore
    @TableField(exist = false)
    private String startTime;

    /**
     * 结束时间
     */
    @JsonIgnore
    @TableField(exist = false)
    private String endTime;
}
