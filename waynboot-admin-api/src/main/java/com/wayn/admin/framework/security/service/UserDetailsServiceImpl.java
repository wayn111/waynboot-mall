package com.wayn.admin.framework.security.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.admin.framework.security.model.LoginUserDetail;
import com.wayn.common.core.entity.system.User;
import com.wayn.common.core.service.system.IDeptService;
import com.wayn.common.core.service.system.IUserService;
import com.wayn.util.enums.UserStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private IUserService iUserService;

    private IDeptService iDeptService;

    private PermissionService permissionService;

    public static void main(String[] args) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.out.println(bCryptPasswordEncoder.encode("123456"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 读取数据库中当前用户信息
        User user = iUserService.getOne(new QueryWrapper<User>().eq("user_name", username));
        // 2. 判断该用户是否存在
        if (user == null) {
            log.info("登录用户：{} 不存在.", username);
            throw new UsernameNotFoundException("登录用户：" + username + " 不存在");
        }
        // 3. 判断是否禁用
        if (Objects.equals(UserStatusEnum.DISABLE.getCode(), user.getUserStatus())) {
            log.info("登录用户：{} 已经被停用.", username);
            throw new DisabledException("登录用户：" + username + " 不存在");
        }
        user.setDept(iDeptService.getById(user.getDeptId()));
        // 4. 获取当前用户的角色信息
        Set<String> rolePermission = permissionService.getRolePermission(user);
        // 5. 根据角色获取权限信息
        Set<String> menuPermission = permissionService.getMenuPermission(rolePermission);
        return new LoginUserDetail(user, menuPermission);
    }
}
