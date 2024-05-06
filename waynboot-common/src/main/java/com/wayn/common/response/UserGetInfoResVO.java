package com.wayn.common.response;

import com.wayn.common.core.entity.system.Role;
import com.wayn.common.core.entity.system.User;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 获取用户详情
 */
@Data
public class UserGetInfoResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5306081689433649990L;

    /**
     * 用户信息
     */
    private User user;

    /**
     * 角色id列表
     */
    private List<Integer> roleIds;

    /**
     * 角色列表
     */
    private List<Role> roles;
}
