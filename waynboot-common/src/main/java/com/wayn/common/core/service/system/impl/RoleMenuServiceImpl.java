package com.wayn.common.core.service.system.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.system.RoleMenu;
import com.wayn.common.core.mapper.system.RoleMenuMapper;
import com.wayn.common.core.service.system.IRoleMenuService;
import org.springframework.stereotype.Service;

@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements IRoleMenuService {
}
