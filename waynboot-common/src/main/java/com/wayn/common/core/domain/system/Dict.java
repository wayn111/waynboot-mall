package com.wayn.common.core.domain.system;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.BaseEntity;
import com.wayn.common.enums.domain.StatusConverter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("sys_dict")
@EqualsAndHashCode(callSuper = true)
public class Dict extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 2263418671720743896L;

    /**
     * 菜单ID
     */
    @TableId(type = IdType.AUTO)
    @ExcelProperty(value = "字典编号")
    private Long dictId;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    @ExcelProperty(value = "字典名称")
    private String name;

    /**
     * 字典值
     */
    @NotBlank(message = "字典类型不能为空")
    @ExcelProperty(value = "字典类型")
    private String value;

    /**
     * 字典状态（0正常 1停用）
     */
    @ExcelProperty(value = "字典状态", converter = StatusConverter.class)
    private Integer dictStatus;

    /**
     * 字典类型（1字典类型  2类型数据）
     */
    @ExcelIgnore
    private Integer type;

    /**
     * 字典排序
     */
    @DecimalMin(value = "0", message = "字典排序不能小于0")
    @ExcelProperty(value = "字典排序")
    private Integer sort;

    @ExcelIgnore
    private String parentType;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @ExcelIgnore
    private Boolean delFlag;

}
