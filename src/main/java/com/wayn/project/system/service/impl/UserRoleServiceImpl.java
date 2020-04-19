package com.wayn.project.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.project.system.domain.SysUserRole;
import com.wayn.project.system.mapper.UserRoleMapper;
import com.wayn.project.system.service.IUserRoleService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, SysUserRole> implements IUserRoleService {
}
