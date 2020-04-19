package com.wayn.project.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.project.system.domain.SysUser;
import com.wayn.project.system.mapper.UserMapper;
import com.wayn.project.system.service.IUserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements IUserService {
}
