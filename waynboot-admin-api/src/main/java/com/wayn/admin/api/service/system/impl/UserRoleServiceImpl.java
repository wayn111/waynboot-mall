package com.wayn.admin.api.service.system.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.admin.api.domain.system.SysUserRole;
import com.wayn.admin.api.mapper.system.UserRoleMapper;
import com.wayn.admin.api.service.system.IUserRoleService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, SysUserRole> implements IUserRoleService {
}
