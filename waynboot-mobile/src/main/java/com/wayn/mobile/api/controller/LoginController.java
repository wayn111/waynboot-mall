package com.wayn.mobile.api.controller;

import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.model.LoginObj;
import com.wayn.common.util.R;
import com.wayn.mobile.framework.security.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class LoginController {
    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public R login(@RequestBody LoginObj loginObj) {
        // 生成令牌
        String token = loginService.login(loginObj.getUsername(), loginObj.getPassword());
        return R.success().add(SysConstants.TOKEN, token);
    }

}
