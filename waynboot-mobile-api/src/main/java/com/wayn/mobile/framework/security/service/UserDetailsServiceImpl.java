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
    public UserDetails loadUserByUsername(String mobile) throws UsernameNotFoundException {
        Member member = iMemberService.getOne(new QueryWrapper<Member>().eq("mobile", mobile));
        if (member == null) {
            log.info("登录用户：{} 不存在.", mobile);
            throw new UsernameNotFoundException("登录用户：" + mobile + " 不存在");
        }
        if (UserStatus.DISABLE.getCode().equals(member.getStatus())) {
            log.info("登录用户：{} 已经被停用.", mobile);
            throw new DisabledException("登录用户：" + mobile + " 不存在");
        }
        return new LoginUserDetail(member, Collections.emptySet());
    }

}
