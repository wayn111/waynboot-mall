package com.wayn.common.core.domain.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门表 sys_dept
 */
@Data
@ApiModel("部门实体")
@TableName("sys_dept")
@EqualsAndHashCode(callSuper = true)
public class Dept extends BaseEntity {

    private static final long serialVersionUID = -1284670591734055472L;
    /**
     * 部门ID
     */
    @TableId(type = IdType.AUTO)
    private Long deptId;

    /**
     * 父部门ID
     */
    @DecimalMin(value = "0", message = "父部门id不能小于0")
    private Long parentId;

    /**
     * 祖级列表
     */
    private String ancestors;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    /**
     * 显示顺序
     */
    @DecimalMin(value = "0", message = "显示顺序不能小于0")
    private Integer sort;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    @Size(min = 0, max = 11, message = "联系电话长度不能超过11个字符")
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    /**
     * 部门状态:0正常,1停用
     */
    private Integer deptStatus;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;

    /**
     * 子部门
     */
    @TableField(exist = false)
    private List<Dept> children = new ArrayList<Dept>();
}
