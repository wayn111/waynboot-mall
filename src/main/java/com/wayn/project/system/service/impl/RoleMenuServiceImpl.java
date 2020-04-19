package com.wayn.project.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.project.system.domain.SysRoleMenu;
import com.wayn.project.system.mapper.RoleMenuMapper;
import com.wayn.project.system.service.IRoleMenuService;
import org.springframework.stereotype.Service;

@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, SysRoleMenu> implements IRoleMenuService {
}
