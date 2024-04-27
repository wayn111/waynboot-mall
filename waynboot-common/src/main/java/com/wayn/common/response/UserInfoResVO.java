package com.wayn.common.response;

import com.wayn.common.core.entity.system.User;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * @author: waynaqua
 * @date: 2024/4/27 15:38
 */
@Data
public class UserInfoResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5306081689433649990L;

    /**
     * 用户信息
     */
    private User user;
    /**
     * 角色权限
     */
    private Set<String> roles;
    /**
     * 菜单权限
     */
    private Set<String> permissions;
}
