package com.wayn.common.core.domain.system;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色和菜单关联 sys_role_menu
 */
@Data
@TableName("sys_role_menu")
public class RoleMenu {
    /**
     * 角色ID
     */
    @TableId
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;

    public RoleMenu(Long roleId, Long menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }
}
