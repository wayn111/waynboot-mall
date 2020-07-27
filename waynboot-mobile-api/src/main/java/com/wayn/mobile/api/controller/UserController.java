package com.wayn.mobile.api.controller;

import com.wayn.common.util.R;
import com.wayn.common.util.ServletUtils;
import com.wayn.mobile.framework.security.LoginUserDetail;
import com.wayn.mobile.framework.security.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("info")
    public R getInfo() {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        return R.success().add("info", loginUser.getMember());
    }
}
