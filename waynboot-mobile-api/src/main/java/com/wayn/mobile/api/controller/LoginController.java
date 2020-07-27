package com.wayn.mobile.api.controller;

import com.wayn.common.constant.SysConstants;
import com.wayn.common.util.R;
import com.wayn.mobile.framework.security.LoginObj;
import com.wayn.mobile.framework.security.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public R login(@RequestBody LoginObj loginObj) {
        String token = loginService.login(loginObj.getMobile(), loginObj.getPassword());
        // 生成令牌
        return R.success().add(SysConstants.TOKEN, token);
    }

}
