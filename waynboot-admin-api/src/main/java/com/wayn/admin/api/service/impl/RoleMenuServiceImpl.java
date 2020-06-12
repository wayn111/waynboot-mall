package com.wayn.admin.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.admin.api.domain.SysRoleMenu;
import com.wayn.admin.api.mapper.RoleMenuMapper;
import com.wayn.admin.api.service.IRoleMenuService;
import org.springframework.stereotype.Service;

@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, SysRoleMenu> implements IRoleMenuService {
}
