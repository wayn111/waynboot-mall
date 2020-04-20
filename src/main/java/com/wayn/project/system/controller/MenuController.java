package com.wayn.project.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.BaseController;
import com.wayn.common.util.R;
import com.wayn.common.util.ServletUtils;
import com.wayn.framework.security.LoginUserDetail;
import com.wayn.framework.security.service.TokenService;
import com.wayn.project.system.domain.SysMenu;
import com.wayn.project.system.service.IMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("system/menu")
public class MenuController extends BaseController {

    @Autowired
    private IMenuService iMenuService;

    @Autowired
    private TokenService tokenService;

    @GetMapping("/list")
    public R list(SysMenu menu) {
        Page<SysMenu> page = getPage();
        return R.success().add("page", iMenuService.listPage(page, menu));
    }

    /**
     * 获取菜单树列表
     */
    @GetMapping("/treeselect")
    public R treeselect(SysMenu menu) {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<SysMenu> menus = iMenuService.selectMenuList(menu, userId);
        return R.success().add("menuTree", iMenuService.buildMenuTreeSelect(menus));
    }

    /**
     * 获取菜单树列表
     */
    @GetMapping("/roleMenuTreeselect/{roleId}")
    public R roleMenuTreeselect(@PathVariable Long roleId) {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<SysMenu> menus = iMenuService.selectMenuList(null, userId);
        return R.success().add("menuTree", iMenuService.buildMenuTreeSelect(menus)).add("checkedKeys", iMenuService.selectCheckedkeys(roleId));
    }

}
