package com.wayn.project.system.controller;

import com.wayn.common.base.BaseController;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.util.R;
import com.wayn.common.util.SecurityUtils;
import com.wayn.common.util.ServletUtils;
import com.wayn.framework.security.LoginUserDetail;
import com.wayn.framework.security.service.TokenService;
import com.wayn.project.system.domain.SysMenu;
import com.wayn.project.system.service.IMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@Api(value = "菜单接口")
@RestController
@RequestMapping("system/menu")
public class MenuController extends BaseController {

    @Autowired
    private IMenuService iMenuService;

    @Autowired
    private TokenService tokenService;

    @ApiOperation(value = "菜单列表", notes = "菜单列表")
    @GetMapping("/list")
    public R list(SysMenu menu) {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<SysMenu> menus = iMenuService.selectMenuList(menu, userId);
        return R.success().add("data", menus);
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

    @ApiOperation(value = "保存菜单", notes = "保存菜单")
    @PostMapping
    public R addRole(@Validated @RequestBody SysMenu menu) {
        if (SysConstants.NOT_UNIQUE.equals(iMenuService.checkMenuNameUnique(menu))) {
            return R.error("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        }
        menu.setCreateBy(SecurityUtils.getUsername());
        menu.setCreateTime(new Date());
        return R.result(iMenuService.save(menu));
    }

    @ApiOperation(value = "更新菜单", notes = "更新菜单")
    @PutMapping
    public R updateRole(@Validated @RequestBody SysMenu menu) {
        if (SysConstants.NOT_UNIQUE.equals(iMenuService.checkMenuNameUnique(menu))) {
            return R.error("更新菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        }
        menu.setUpdateBy(SecurityUtils.getUsername());
        menu.setUpdateTime(new Date());
        return R.result(iMenuService.updateById(menu));
    }

    @ApiOperation(value = "获取菜单详细", notes = "获取菜单详细")
    @GetMapping("/{menuId}")
    public R getMenu(@PathVariable Long menuId) {
        return R.success().add("data", iMenuService.getById(menuId));
    }

    @ApiOperation(value = "删除菜单", notes = "删除菜单")
    @DeleteMapping("/{menuId}")
    public R deleteMenu(@PathVariable Long menuId) {
        if (iMenuService.hasChildByMenuId(menuId)) {
            return R.error("存在子菜单,不允许删除");
        }
        if (iMenuService.checkMenuExistRole(menuId)) {
            return R.error("菜单已分配,不允许删除");
        }
        return R.success().add("data", iMenuService.removeById(menuId));
    }

}
