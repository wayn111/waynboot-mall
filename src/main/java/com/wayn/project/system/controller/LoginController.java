package com.wayn.project.system.controller;

import com.wayn.common.constant.SysConstants;
import com.wayn.common.util.R;
import com.wayn.framework.security.LoginObj;
import com.wayn.framework.security.LoginUserDetail;
import com.wayn.framework.security.service.LoginService;
import com.wayn.framework.security.service.SysPermissionService;
import com.wayn.framework.security.service.TokenService;
import com.wayn.project.system.domain.SysMenu;
import com.wayn.project.system.domain.SysUser;
import com.wayn.project.system.service.IMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysPermissionService sysPermissionService;

    @Autowired
    private IMenuService iMenuService;

    @PostMapping("/login")
    public R login(@RequestBody LoginObj loginObj) {
        R success = R.success();
        // 生成令牌
        String token = loginService.login(loginObj.getUsername(), loginObj.getPassword(), loginObj.getCode());
        return success.add(SysConstants.TOKEN, token);
    }

    @GetMapping("/getInfo")
    public R userInfo(HttpServletRequest request) {
        R success = R.success();
        LoginUserDetail loginUser = tokenService.getLoginUser(request);
        SysUser user = loginUser.getUser();
        Set<String> rolePermission = sysPermissionService.getRolePermission(user);
        Set<String> menuPermission = sysPermissionService.getMenuPermission(user);
        success.add("user", user);
        success.add("roles", rolePermission);
        success.add("permissions", menuPermission);
        return success;
    }


    @GetMapping("/getRouters")
    public R getRouters(HttpServletRequest request) {
        R success = R.success();
        LoginUserDetail loginUser = tokenService.getLoginUser(request);
        // 用户信息
        SysUser user = loginUser.getUser();
        List<SysMenu> menus = iMenuService.selectMenuTreeByUserId(user.getUserId());
        return success.add("routers", iMenuService.buildMenus(menus));
    }
}
