package com.wayn.common.core.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.system.Role;
import com.wayn.common.core.domain.system.RoleMenu;
import com.wayn.common.core.domain.system.UserRole;
import com.wayn.common.core.mapper.system.RoleMapper;
import com.wayn.common.core.service.system.IRoleMenuService;
import com.wayn.common.core.service.system.IRoleService;
import com.wayn.common.core.service.system.IUserRoleService;
import com.wayn.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Autowired
    private RoleMapper roleMapper;


    @Autowired
    private IRoleMenuService iRoleMenuService;

    @Autowired
    private IUserRoleService iUserRoleService;

    @Override
    public List<String> selectRoleByUserId(Long userId) {
        return roleMapper.selectRoleByUserId(userId);
    }

    @Override
    public List<Integer> selectRoleListByUserId(Long userId) {
        return roleMapper.selectRoleListByUserId(userId);
    }

    @Override
    public String checkRoleNameUnique(Role role) {
        long roleId = Objects.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        Role sysRole = getOne(new QueryWrapper<Role>().eq("role_name", role.getRoleName()));
        if (sysRole != null && sysRole.getRoleId() != roleId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public String checkRoleKeyUnique(Role role) {
        long roleId = Objects.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        Role sysRole = getOne(new QueryWrapper<Role>().eq("role_key", role.getRoleKey()));
        if (sysRole != null && sysRole.getRoleId() != roleId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public void checkRoleAllowed(Role role) {
        if (Objects.nonNull(role.getRoleId()) && role.isAdmin()) {
            throw new BusinessException("不允许操作管理员角色");
        }
    }

    @Transactional
    @Override
    public boolean insertRoleAndMenu(Role role) {
        save(role);
        List<RoleMenu> roleMenus = role.getMenuIds().stream().map(menuId -> new RoleMenu(role.getRoleId(), menuId)).collect(Collectors.toList());
        return roleMenus.isEmpty() || iRoleMenuService.saveBatch(roleMenus);
    }

    @Transactional
    @Override
    public boolean updateRoleAndMenu(Role role) {
        updateById(role);
        iRoleMenuService.remove(new QueryWrapper<RoleMenu>().eq("role_id", role.getRoleId()));
        List<RoleMenu> roleMenus = role.getMenuIds().stream().map(menuId -> new RoleMenu(role.getRoleId(), menuId)).collect(Collectors.toList());
        return roleMenus.isEmpty() || iRoleMenuService.saveBatch(roleMenus);
    }

    @Override
    public boolean deleteRoleByIds(List<Long> roleIds) {
        for (Long roleId : roleIds) {
            checkRoleAllowed(new Role(roleId));
            Role role = getById(roleId);
            int count = countUserRoleByRoleId(roleId);
            if (count > 0) {
                throw new BusinessException(String.format("%1$s已分配,不能删除", role.getRoleName()));
            }
        }
        return removeByIds(roleIds);
    }

    @Override
    public int countUserRoleByRoleId(Long roleId) {
        return iUserRoleService.count(new QueryWrapper<UserRole>().eq("role_id", roleId));
    }

    @Override
    public IPage<Role> listPage(Page<Role> page, Role role) {
        return roleMapper.selectRoleListPage(page, role);
    }

    @Override
    public List<Role> list(Role role) {
        return roleMapper.selectRoleList(role);
    }
}
