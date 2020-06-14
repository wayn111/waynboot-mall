package com.wayn.admin.api.service.system.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.admin.api.domain.system.SysRoleMenu;
import com.wayn.admin.api.mapper.system.RoleMenuMapper;
import com.wayn.admin.api.service.system.IRoleMenuService;
import org.springframework.stereotype.Service;

@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, SysRoleMenu> implements IRoleMenuService {
}
