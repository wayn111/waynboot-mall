package com.wayn.common.core.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.system.Role;

import java.util.List;

public interface RoleMapper extends BaseMapper<Role> {

    IPage<Role> selectRoleListPage(Page<Role> page, Role role);

    List<String> selectRoleByUserId(Long userId);

    List<Integer> selectRoleListByUserId(Long userId);

    List<Role> selectRoleList(Role role);

    List<Role> selectRolesByUserName(String userName);
}
