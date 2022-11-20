package com.wayn.common.base.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 基础entity类，集成以获取通用字段
 */
@Data
public class BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1956057929467119856L;
    /**
     * 创建者
     */
    @ExcelIgnore
    private String createBy;
    /**
     * 创建时间
     */
    @DateTimeFormat("yyyy年MM月dd日HH时mm分ss秒")
    @ExcelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


    /**
     * 更新者
     */
    @ExcelIgnore
    private String updateBy;

    /**
     * 更新时间
     */
    @ExcelIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 开始时间
     */
    @JsonIgnore
    @TableField(exist = false)
    @ExcelIgnore
    private String startTime;

    /**
     * 结束时间
     */
    @JsonIgnore
    @TableField(exist = false)
    @ExcelIgnore
    private String endTime;
}
