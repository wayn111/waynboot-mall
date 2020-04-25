package com.wayn.project.system.domain;

import lombok.Data;

/**
 * 用户和角色关联 sys_user_role
 */
@Data
public class SysUserRole {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    public SysUserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public SysUserRole() {
    }
}
