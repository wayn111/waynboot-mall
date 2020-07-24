package com.wayn.common.core.service.system.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.system.UserRole;
import com.wayn.common.core.mapper.system.UserRoleMapper;
import com.wayn.common.core.service.system.IUserRoleService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {
}
