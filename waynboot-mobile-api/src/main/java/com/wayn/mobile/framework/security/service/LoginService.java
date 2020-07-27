package com.wayn.mobile.framework.security.service;

import com.wayn.common.exception.BusinessException;
import com.wayn.mobile.framework.security.LoginUserDetail;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;


    @SneakyThrows
    public String login(String mobile, String password) {
        // 用户验证
        Authentication authentication;
        try {
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(mobile, password));
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                throw new BadCredentialsException(e.getMessage(), e);
            } else {
                throw new BusinessException(e.getMessage());
            }
        }
        LoginUserDetail principal = (LoginUserDetail) authentication.getPrincipal();
        return tokenService.createToken(principal);
    }
}
