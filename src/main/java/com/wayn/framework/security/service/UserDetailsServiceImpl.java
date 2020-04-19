package com.wayn.framework.security.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.framework.security.LoginUserDetail;
import com.wayn.project.system.domain.SysUser;
import com.wayn.project.system.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.out.println(bCryptPasswordEncoder.encode("123456"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser dbUser = iUserService.getOne(new QueryWrapper<SysUser>().eq("user_name", username));
        if (dbUser == null) {
//            List<GrantedAuthority> authorityLists = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");
            log.info("登录用户：{} 不存在.", username);
            throw new UsernameNotFoundException("登录用户：" + username + " 不存在");
        }
        return new LoginUserDetail(dbUser);
    }

}
