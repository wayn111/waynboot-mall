package com.wayn.admin.api.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.api.domain.system.User;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {


    IPage<User> selectUserListPage(Page<User> page, User user);

    List<User> selectUserList(User user);
}
