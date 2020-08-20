package com.wayn.common.core.domain.system;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户和角色关联 sys_user_role
 */
@Data
@TableName("sys_user_role")
public class UserRole {
    /**
     * 用户ID
     */
    @TableId
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public UserRole() {
    }
}
