package com.wayn.mobile.framework.security.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.core.domain.shop.Member;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.common.enums.UserStatus;
import com.wayn.mobile.framework.security.LoginUserDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private IMemberService iMemberService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = iMemberService.getOne(new QueryWrapper<Member>().eq("username", username));
        if (member == null) {
            log.info("登录用户：{} 不存在.", username);
            throw new UsernameNotFoundException("登录用户：" + username + " 不存在");
        }
        if (UserStatus.DISABLE.getCode() == member.getStatus()) {
            log.info("登录用户：{} 已经被停用.", username);
            throw new DisabledException("登录用户：" + username + " 不存在");
        }
        return new LoginUserDetail(member, Collections.emptySet());
    }

}
