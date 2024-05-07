package com.wayn.admin.framework.security.service;

import com.wayn.admin.framework.security.model.LoginUserDetail;
import com.wayn.common.core.entity.system.Role;
import com.wayn.common.core.entity.system.User;
import com.wayn.common.core.service.system.IMenuService;
import com.wayn.common.core.service.system.IRoleService;
import com.wayn.common.core.service.system.IUserService;
import com.wayn.util.constant.SysConstants;
import com.wayn.util.util.ServletUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service("ss")
@AllArgsConstructor
public class PermissionService {
    private TokenService tokenService;
    private IUserService userService;
    private IRoleService roleService;
    private IMenuService menuService;

    /**
     * 验证用户是否具备某权限
     *
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    public boolean hasPermi(String permission) {
        if (StringUtils.isEmpty(permission)) {
            return false;
        }
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        if (Objects.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getPermissions())) {
            return false;
        }
        return hasPermissions(loginUser.getPermissions(), permission);
    }

    /**
     * 验证用户是否不具备某权限，与 hasPermi逻辑相反
     *
     * @param permission 权限字符串
     * @return 用户是否不具备某权限
     */
    public boolean lacksPermi(String permission) {
        return !hasPermi(permission);
    }

    /**
     * 验证用户是否具有以下任意一个权限
     *
     * @param permissions 以 PERMISSION_NAMES_DELIMETER 为分隔符的权限列表
     * @return 用户是否具有以下任意一个权限
     */
    public boolean hasAnyPermi(String permissions) {
        if (StringUtils.isEmpty(permissions)) {
            return false;
        }
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        if (Objects.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getPermissions())) {
            return false;
        }
        Set<String> authorities = loginUser.getPermissions();
        for (String permission : permissions.split(SysConstants.PERMISSION_DELIMETER)) {
            if (permission != null && hasPermissions(authorities, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户是否拥有某个角色
     *
     * @param role 角色字符串
     * @return 用户是否具备某角色
     */
    public boolean hasRole(String role) {
        if (StringUtils.isEmpty(role)) {
            return false;
        }
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        if (Objects.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getUser().getRoles())) {
            return false;
        }
        for (Role sysRole : loginUser.getUser().getRoles()) {
            String roleKey = sysRole.getRoleKey();
            if (SysConstants.SUPER_ADMIN.contains(roleKey) || roleKey.contains(StringUtils.trim(role))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证用户是否不具备某角色，与 isRole逻辑相反。
     *
     * @param role 角色名称
     * @return 用户是否不具备某角色
     */
    public boolean lacksRole(String role) {
        return !hasRole(role);
    }

    /**
     * 验证用户是否具有以下任意一个角色
     *
     * @param roles 以 ROLE_NAMES_DELIMETER 为分隔符的角色列表
     * @return 用户是否具有以下任意一个角色
     */
    public boolean hasAnyRoles(String roles) {
        if (StringUtils.isEmpty(roles)) {
            return false;
        }
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        if (Objects.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getUser().getRoles())) {
            return false;
        }
        for (String role : roles.split(SysConstants.ROLE_DELIMETER)) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否包含权限
     *
     * @param permissions 权限列表
     * @param permission  权限字符串
     * @return 用户是否具备某权限
     */
    private boolean hasPermissions(Set<String> permissions, String permission) {
        return permissions.contains(SysConstants.ALL_PERMISSION)
                || permissions.contains(StringUtils.trim(permission));
    }

    /**
     * 获取角色数据权限
     *
     * @param user 用户信息
     * @return 角色权限信息
     */
    public Set<String> getRolePermission(User user) {
        Set<String> roles = new HashSet<>();
        // 管理员拥有所有权限
        if (user.isAdmin()) {
            roles.add("admin");
        } else {
            roles.addAll(roleService.selectRoleKeyByUserId(user.getUserId()));
        }
        return roles;
    }

    /**
     * 获取菜单数据权限
     *
     * @param roleKeys 角色信息
     * @return 菜单权限信息
     */
    public Set<String> getMenuPermission(Set<String> roleKeys) {
        Set<String> perms = new HashSet<>();
        // 管理员拥有所有权限
        if (roleKeys.contains(SysConstants.SUPER_ADMIN)) {
            perms.add("*:*:*");
        } else {
            for (String roleKey : roleKeys) {
                perms.addAll(menuService.selectMenuPermsByRoleKey(roleKey));
            }
        }
        return perms;
    }
}
