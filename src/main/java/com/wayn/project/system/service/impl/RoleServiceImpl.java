package com.wayn.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.exception.BusinessException;
import com.wayn.project.system.domain.SysRole;
import com.wayn.project.system.domain.SysRoleMenu;
import com.wayn.project.system.domain.SysUserRole;
import com.wayn.project.system.mapper.RoleMapper;
import com.wayn.project.system.service.IRoleMenuService;
import com.wayn.project.system.service.IRoleService;
import com.wayn.project.system.service.IUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, SysRole> implements IRoleService {

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
    public String checkRoleNameUnique(SysRole role) {
        long roleId = Objects.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole sysRole = getOne(new QueryWrapper<SysRole>().eq("role_name", role.getRoleName()));
        if (sysRole != null && sysRole.getRoleId() != roleId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public String checkRoleKeyUnique(SysRole role) {
        long roleId = Objects.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole sysRole = getOne(new QueryWrapper<SysRole>().eq("role_key", role.getRoleKey()));
        if (sysRole != null && sysRole.getRoleId() != roleId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public void checkRoleAllowed(SysRole role) {
        if (Objects.nonNull(role.getRoleId()) && role.isAdmin()) {
            throw new BusinessException("不允许操作管理员角色");
        }
    }

    @Transactional
    @Override
    public boolean insertRoleAndMenu(SysRole role) {
        save(role);
        List<SysRoleMenu> roleMenus = role.getMenuIds().stream().map(menuId -> new SysRoleMenu(role.getRoleId(), menuId)).collect(Collectors.toList());
        return roleMenus.isEmpty() || iRoleMenuService.saveBatch(roleMenus);
    }

    @Transactional
    @Override
    public boolean updateRoleAndMenu(SysRole role) {
        updateById(role);
        iRoleMenuService.remove(new QueryWrapper<SysRoleMenu>().eq("role_id", role.getRoleId()));
        List<SysRoleMenu> roleMenus = role.getMenuIds().stream().map(menuId -> new SysRoleMenu(role.getRoleId(), menuId)).collect(Collectors.toList());
        return roleMenus.isEmpty() || iRoleMenuService.saveBatch(roleMenus);
    }

    @Override
    public boolean deleteRoleByIds(List<Long> roleIds) {
        for (Long roleId : roleIds) {
            checkRoleAllowed(new SysRole(roleId));
            SysRole sysRole = getById(roleId);
            int count = countUserRoleByRoleId(roleId);
            if (count > 0) {
                throw new BusinessException(String.format("%1$s已分配,不能删除", sysRole.getRoleName()));
            }
        }
        return removeByIds(roleIds);
    }

    @Override
    public int countUserRoleByRoleId(Long roleId) {
        return iUserRoleService.count(new QueryWrapper<SysUserRole>().eq("role_id", roleId));
    }

    @Override
    public IPage<SysRole> listPage(Page<SysRole> page, SysRole role) {
        return roleMapper.selectRoleListPage(page, role);
    }

    @Override
    public List<SysRole> list(SysRole role) {
        return roleMapper.selectRoleList(role);
    }
}
