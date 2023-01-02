package com.wayn.common.core.service.system.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.system.Role;
import com.wayn.common.core.domain.system.User;
import com.wayn.common.core.domain.system.UserRole;
import com.wayn.common.core.mapper.system.RoleMapper;
import com.wayn.common.core.mapper.system.UserMapper;
import com.wayn.common.core.service.system.IUserRoleService;
import com.wayn.common.core.service.system.IUserService;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.util.R;
import com.wayn.common.util.security.SecurityUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private UserMapper userMapper;

    private RoleMapper roleMapper;

    private IUserRoleService iUserRoleService;

    @Override
    public IPage<User> listPage(Page<User> page, User user) {
        return userMapper.selectUserListPage(page, user);
    }

    @Override
    public void checkUserAllowed(User user) {
        if (user.isAdmin()) {
            throw new BusinessException("不允许操作管理员用户");
        }
    }

    @Override
    public String checkUserNameUnique(String userName) {
        long count = count(Wrappers.lambdaQuery(User.class).eq(User::getUserName, userName));
        if (count > 0) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public String checkPhoneUnique(User user) {
        long userId = Objects.nonNull(user.getUserId()) ? user.getUserId() : 0;
        User info = getOne(new QueryWrapper<User>().eq("phone", user.getPhone()));
        if (info != null && info.getUserId() != userId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public String checkEmailUnique(User user) {
        long userId = Objects.nonNull(user.getUserId()) ? user.getUserId() : 0;
        User info = getOne(new QueryWrapper<User>().eq("email", user.getEmail()));
        if (info != null && info.getUserId() != userId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public boolean insertUserAndRole(User user) {
        save(user);
        List<UserRole> userRoles = Arrays.stream(user.getRoleIds()).map(item -> new UserRole(user.getUserId(), item)).collect(Collectors.toList());
        return userRoles.isEmpty() || iUserRoleService.saveBatch(userRoles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserAndRole(User user) {
        updateById(user);
        iUserRoleService.remove(new QueryWrapper<UserRole>().eq("user_id", user.getUserId()));
        List<UserRole> userRoles = Arrays.stream(user.getRoleIds()).map(item -> new UserRole(user.getUserId(), item)).collect(Collectors.toList());
        return userRoles.isEmpty() || iUserRoleService.saveBatch(userRoles);
    }

    @Override
    public List<User> list(User user) {
        return userMapper.selectUserList(user);
    }

    @Override
    public String selectUserRoleGroup(String userName) {
        List<Role> list = roleMapper.selectRolesByUserName(userName);
        StringBuilder idsStr = new StringBuilder();
        for (Role role : list) {
            idsStr.append(role.getRoleName()).append(",");
        }
        if (StringUtils.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public R importUser(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            List<User> list = new ArrayList<>();
            EasyExcel.read(inputStream, User.class, new PageReadListener<User>(dataList -> {
                for (User user : dataList) {
                    if (SysConstants.NOT_UNIQUE.equals(this.checkUserNameUnique(user.getUserName()))) {
                        throw new BusinessException(String.format("导入用户[%s]失败，登录账号已存在", user.getUserName()));
                    } else if (SysConstants.NOT_UNIQUE.equals(this.checkPhoneUnique(user))) {
                        throw new BusinessException(String.format("导入用户[%s]失败，手机号码已存在", user.getUserName()));
                    } else if (SysConstants.NOT_UNIQUE.equals(this.checkEmailUnique(user))) {
                        throw new BusinessException(String.format("导入用户[%s]失败，邮箱账号已存在", user.getUserName()));
                    }
                    user.setDeptId(101L);
                    user.setCreateBy(SecurityUtils.getUsername());
                    user.setCreateTime(new Date());
                    user.setPassword(SecurityUtils.encryptPassword(SysConstants.DEFAULT_PASSWORD));
                }
                this.saveBatch(list);
            })).sheet().doRead();
            return R.success();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
