package com.wayn.project.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.project.system.domain.SysRole;

import java.util.List;

public interface RoleMapper extends BaseMapper<SysRole> {

    IPage<SysRole> selectRoleListPage(Page<SysRole> page, SysRole role);

    List<String> selectRoleByUserId(Long userId);

    List<Integer> selectRoleListByUserId(Long userId);

    List<SysRole> selectRoleList(SysRole role);

    List<SysRole> selectRolesByUserName(String userName);
}
