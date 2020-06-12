package com.wayn.admin.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.api.domain.SysUser;

import java.util.List;

public interface UserMapper extends BaseMapper<SysUser> {


    IPage<SysUser> selectUserListPage(Page<SysUser> page, SysUser user);

    List<SysUser> selectUserList(SysUser user);
}
