package com.wayn.common.core.domain.system;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel("角色实体")
@TableName("sys_dict")
@EqualsAndHashCode(callSuper = true)
public class Dict extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 2263418671720743896L;

    /**
     * 菜单ID
     */
    @TableId(type = IdType.AUTO)
    @Excel(name = "字典编号", type = 10)
    private Long dictId;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    @Excel(name = "字典名称")
    private String name;

    /**
     * 字典值
     */
    @NotBlank(message = "字典类型不能为空")
    @Excel(name = "字典类型")
    private String value;

    /**
     * 字典状态（0正常 1停用）
     */
    @Excel(name = "字典状态", replace = {"启用_0", "禁用_1"})
    private Integer dictStatus;

    /**
     * 字典类型（1字典类型  2类型数据）
     */
    private Integer type;

    /**
     * 字典排序
     */
    @DecimalMin(value = "0", message = "字典排序不能小于0")
    @Excel(name = "字典排序", type = 10)
    private Integer sort;

    private String parentType;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;
}
