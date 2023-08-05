package com.wayn.common.core.domain.system;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.BaseEntity;
import com.wayn.common.enums.domain.SexConverter;
import com.wayn.common.enums.domain.StatusConverter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;
import java.util.Set;

/**
 * 用户表 sys_user
 */
@Data
@TableName("sys_user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -8079172156772887677L;
    /**
     * 用户id
     */
    @TableId(type = IdType.AUTO)
    @ExcelProperty(value = "用户ID")
    private Long userId;

    /**
     * 用户名
     */
    @ExcelProperty(value = "用户名称")
    @NotBlank(message = "用户名不能为空")
    private String userName;

    /**
     * 用户昵称
     */
    @ExcelProperty(value = "用户昵称")
    @NotBlank(message = "用户名不能为空")
    private String nickName;

    /**
     * 密码
     */
    @Size(min = 6, max = 50, message = "密码长度不能低于6个字符")
    @ExcelIgnore
    private String password;

    /**
     * 性别
     */
    @ExcelProperty(value = "用户性别", converter = SexConverter.class)
    private Integer sex;

    /**
     * 用户状态 0 启用 1 禁用
     */
    @ExcelProperty(value = "用户状态", converter = StatusConverter.class)
    private Integer userStatus;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    @ExcelProperty(value = "邮箱")
    private String email;

    /**
     * 手机号码
     */
    @Size(min = 0, max = 11, message = "联系电话长度不能超过11个字符")
    @ExcelProperty(value = "手机号码")
    private String phone;

    /**
     * 关联部门id
     */
    @ExcelIgnore
    private Long deptId;

    /**
     * 关联部门名称
     */
    @TableField(exist = false)
    @ExcelProperty(value = "部门名称")
    private String deptName;

    /**
     * 用户头像
     */
    @ExcelIgnore
    private String avatar;

    @TableField(exist = false)
    @ExcelIgnore
    private Dept dept;

    /**
     * 角色对象
     */
    @TableField(exist = false)
    @ExcelIgnore
    private List<Role> roles;

    /**
     * 角色组
     */
    @TableField(exist = false)
    @ExcelIgnore
    private Long[] roleIds;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @ExcelIgnore
    private Boolean delFlag;

    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    public boolean isAdmin() {
        return isAdmin(this.userId);
    }

}
