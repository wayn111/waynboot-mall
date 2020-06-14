package com.wayn.admin.api.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.admin.api.domain.system.SysRole;
import com.wayn.admin.api.domain.system.SysUser;
import com.wayn.admin.api.domain.system.SysUserRole;
import com.wayn.admin.api.mapper.system.RoleMapper;
import com.wayn.admin.api.mapper.system.UserMapper;
import com.wayn.admin.api.service.system.IUserRoleService;
import com.wayn.admin.api.service.system.IUserService;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private IUserRoleService iUserRoleService;

    @Override
    public IPage<SysUser> listPage(Page<SysUser> page, SysUser user) {
        return userMapper.selectUserListPage(page, user);
    }

    @Override
    public void checkUserAllowed(SysUser user) {
        if (user.isAdmin()) {
            throw new BusinessException("不允许操作管理员用户");
        }
    }

    @Override
    public String checkUserNameUnique(String userName) {
        int count = count(new QueryWrapper<SysUser>().eq("user_name", userName));
        if (count > 0) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public String checkPhoneUnique(SysUser user) {
        long userId = Objects.nonNull(user.getUserId()) ? user.getUserId() : 0;
        SysUser info = getOne(new QueryWrapper<SysUser>().eq("phone", user.getPhone()));
        if (info != null && info.getUserId() != userId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public String checkEmailUnique(SysUser user) {
        long userId = Objects.nonNull(user.getUserId()) ? user.getUserId() : 0;
        SysUser info = getOne(new QueryWrapper<SysUser>().eq("email", user.getEmail()));
        if (info != null && info.getUserId() != userId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public boolean insertUserAndRole(SysUser user) {
        save(user);
        List<SysUserRole> userRoles = Arrays.stream(user.getRoleIds()).map(item -> new SysUserRole(user.getUserId(), item)).collect(Collectors.toList());
        return userRoles.isEmpty() || iUserRoleService.saveBatch(userRoles);
    }

    @Override
    public boolean updateUserAndRole(SysUser user) {
        updateById(user);
        iUserRoleService.remove(new QueryWrapper<SysUserRole>().eq("user_id", user.getUserId()));
        List<SysUserRole> userRoles = Arrays.stream(user.getRoleIds()).map(item -> new SysUserRole(user.getUserId(), item)).collect(Collectors.toList());
        return userRoles.isEmpty() || iUserRoleService.saveBatch(userRoles);
    }

    @Override
    public List<SysUser> list(SysUser user) {
        return userMapper.selectUserList(user);
    }

    @Override
    public String selectUserRoleGroup(String userName) {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        StringBuffer idsStr = new StringBuffer();
        for (SysRole role : list) {
            idsStr.append(role.getRoleName()).append(",");
        }
        if (StringUtils.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }
}
