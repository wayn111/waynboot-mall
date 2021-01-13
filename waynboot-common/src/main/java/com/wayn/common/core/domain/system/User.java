package com.wayn.common.core.domain.system;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wayn.common.base.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户表 sys_user
 */
@Data
@ApiModel("用户实体")
@TableName("sys_user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    private static final long serialVersionUID = -8079172156772887677L;
    /**
     * 用户id
     */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名
     */
    @Excel(name = "用户名称")
    @NotBlank(message = "用户名不能为空")
    private String userName;

    /**
     * 用户昵称
     */
    @Excel(name = "用户昵称")
    @NotBlank(message = "用户名不能为空")
    private String nickName;

    /**
     * 密码
     */
    @Size(min = 6, max = 50, message = "密码长度不能低于6个字符")
    private String password;

    /**
     * 性别
     */
    @Excel(name = "用户性别", type = 10, replace = {"男_0", "女_1"})
    private Integer sex;

    /**
     * 用户状态 0 启用 1 禁用
     */
    @Excel(name = "用户状态", type = 10, replace = {"启用_0", "禁用_1"})
    private Integer userStatus;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    @Excel(name = "邮箱", width = 20)
    private String email;

    /**
     * 手机号码
     */
    @Size(min = 0, max = 11, message = "联系电话长度不能超过11个字符")
    @Excel(name = "手机号码", width = 20)
    private String phone;

    /**
     * 关联部门id
     */
    private Long deptId;

    /**
     * 关联部门名称
     */
    @TableField(exist = false)
    @Excel(name = "部门名称", width = 15)
    private String deptName;

    /**
     * 用户头像
     */
    private String avatar;

    @TableField(exist = false)
    private Dept dept;

    /**
     * 角色对象
     */
    @TableField(exist = false)
    private List<Role> roles;

    /**
     * 角色组
     */
    @TableField(exist = false)
    private Long[] roleIds;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private Boolean delFlag;

    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    public boolean isAdmin() {
        return isAdmin(this.userId);
    }

}
