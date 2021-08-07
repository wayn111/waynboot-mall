package com.wayn.admin.api.controller.system;

import com.wayn.admin.framework.security.service.TokenService;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.system.Menu;
import com.wayn.common.core.model.LoginUserDetail;
import com.wayn.common.core.service.system.IMenuService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import com.wayn.common.util.ServletUtils;
import com.wayn.common.util.security.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @ApiOperation(value = "菜单列表", notes = "菜单列表")
    @GetMapping("/list")
    public R list(Menu menu) {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<Menu> menus = iMenuService.selectMenuList(menu, userId);
        return R.success().add("data", menus);
    }

    /**
     * 获取菜单树列表
     */
    @GetMapping("/treeselect")
    public R treeselect(Menu menu) {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<Menu> menus = iMenuService.selectMenuList(menu, userId);
        return R.success().add("menuTree", iMenuService.buildMenuTreeSelect(menus));
    }

    /**
     * 获取菜单树列表
     */
    @GetMapping("/roleMenuTreeselect/{roleId}")
    public R roleMenuTreeselect(@PathVariable Long roleId) {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<Menu> menus = iMenuService.selectMenuList(new Menu(), userId);
        return R.success().add("menuTree", iMenuService.buildMenuTreeSelect(menus)).add("checkedKeys", iMenuService.selectCheckedkeys(roleId));
    }

    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @ApiOperation(value = "保存菜单", notes = "保存菜单")
    @PostMapping
    public R addRole(@Validated @RequestBody Menu menu) {
        if (SysConstants.NOT_UNIQUE.equals(iMenuService.checkMenuNameUnique(menu))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("新增菜单[%s]失败，菜单名称已存在", menu.getMenuName())));
        }
        menu.setCreateBy(SecurityUtils.getUsername());
        menu.setCreateTime(new Date());
        return R.result(iMenuService.save(menu));
    }

    @PreAuthorize("@ss.hasPermi('system:menu:update')")
    @ApiOperation(value = "更新菜单", notes = "更新菜单")
    @PutMapping
    public R updateRole(@Validated @RequestBody Menu menu) {
        if (SysConstants.NOT_UNIQUE.equals(iMenuService.checkMenuNameUnique(menu))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("更新菜单[%s]失败，菜单名称已存在", menu.getMenuName())));
        }
        menu.setUpdateBy(SecurityUtils.getUsername());
        menu.setUpdateTime(new Date());
        return R.result(iMenuService.updateById(menu));
    }

    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @ApiOperation(value = "获取菜单详细", notes = "获取菜单详细")
    @GetMapping("/{menuId}")
    public R getMenu(@PathVariable Long menuId) {
        return R.success().add("data", iMenuService.getById(menuId));
    }

    @PreAuthorize("@ss.hasPermi('system:menu:delete')")
    @ApiOperation(value = "删除菜单", notes = "删除菜单")
    @DeleteMapping("/{menuId}")
    public R deleteMenu(@PathVariable Long menuId) {
        if (iMenuService.hasChildByMenuId(menuId)) {
            return R.error(ReturnCodeEnum.MENU_HAS_SUB_MENU_ERROR);
        }
        if (iMenuService.checkMenuExistRole(menuId)) {
            return R.error(ReturnCodeEnum.MENU_HAS_DISTRIBUTE_ERROR);
        }
        return R.result(iMenuService.removeById(menuId));
    }
}
