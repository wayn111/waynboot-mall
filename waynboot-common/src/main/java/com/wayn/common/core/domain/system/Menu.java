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
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单权限表 sys_menu
 */
@Data
@ApiModel("菜单实体")
@TableName("sys_menu")
@EqualsAndHashCode(callSuper = true)
public class Menu extends BaseEntity {

    private static final long serialVersionUID = 3675171030764421787L;
    /**
     * 菜单ID
     */
    @TableId(type = IdType.AUTO)
    private Long menuId;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    /**
     * 父菜单id
     */
    @DecimalMin(value = "0", message = "父菜单id不能小于0")
    private Long parentId;

    /**
     * 显示顺序
     */
    @DecimalMin(value = "0", message = "显示顺序不能小于0")
    private Integer sort;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 是否为外链（0是 1否）
     */
    private String isFrame;

    /**
     * 类型（M目录 C菜单 F按钮）
     */
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    /**
     * 菜单状态（0显示 1隐藏）
     */
    private Integer menuStatus;
    /**
     * 显示状态（0显示 1隐藏）
     */
    private Integer visible;

    /**
     * 权限字符串
     */
    private String perms;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 子菜单
     */
    @TableField(exist = false)
    private List<Menu> children = new ArrayList<>();

}
