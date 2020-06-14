package com.wayn.admin.framework.security.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.admin.api.domain.system.SysUser;
import com.wayn.admin.api.service.system.IDeptService;
import com.wayn.admin.api.service.system.IUserService;
import com.wayn.admin.framework.security.LoginUserDetail;
import com.wayn.common.enums.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IDeptService iDeptService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SysPermissionService permissionService;

    public static void main(String[] args) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.out.println(bCryptPasswordEncoder.encode("123456"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = iUserService.getOne(new QueryWrapper<SysUser>().eq("user_name", username));
        if (user == null) {
            log.info("登录用户：{} 不存在.", username);
            throw new UsernameNotFoundException("登录用户：" + username + " 不存在");
        }
        if (UserStatus.DISABLE.getCode() == user.getUserStatus()) {
            log.info("登录用户：{} 已经被停用.", username);
            throw new DisabledException("登录用户：" + username + " 不存在");
        }
        user.setSysDept(iDeptService.getById(user.getDeptId()));
        return new LoginUserDetail(user, permissionService.getMenuPermission(user));
    }

}
