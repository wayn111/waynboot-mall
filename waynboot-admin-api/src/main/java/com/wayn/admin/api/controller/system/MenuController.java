package com.wayn.admin.api.controller.system;

import com.wayn.admin.framework.security.model.LoginUserDetail;
import com.wayn.admin.framework.security.service.TokenService;
import com.wayn.admin.framework.security.util.SecurityUtils;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.system.Menu;
import com.wayn.common.core.service.system.IMenuService;
import com.wayn.common.core.vo.TreeVO;
import com.wayn.common.response.RoleMenuTreeselectResVO;
import com.wayn.util.constant.SysConstants;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
import com.wayn.util.util.ServletUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 后台菜单管理
 *
 * @author wayn
 * @since 2020-07-21
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("system/menu")
public class MenuController extends BaseController {

    private IMenuService iMenuService;

    private TokenService tokenService;

    /**
     * 菜单列表
     *
     * @param menu
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping("/list")
    public R<List<Menu>> list(Menu menu) {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<Menu> menus = iMenuService.selectMenuList(menu, userId);
        return R.success(menus);
    }

    /**
     * 获取菜单树列表
     */
    @GetMapping("/treeselect")
    public R<List<TreeVO>> treeselect(Menu menu) {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<Menu> menus = iMenuService.selectMenuList(menu, userId);
        return R.success(iMenuService.buildMenuTreeSelect(menus));
    }

    /**
     * 获取菜单树列表
     */
    @GetMapping("/roleMenuTreeselect/{roleId}")
    public R<RoleMenuTreeselectResVO> roleMenuTreeselect(@PathVariable Long roleId) {
        LoginUserDetail loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<Menu> menus = iMenuService.selectMenuList(new Menu(), userId);
        RoleMenuTreeselectResVO resVO = new RoleMenuTreeselectResVO();
        resVO.setMenuTree(iMenuService.buildMenuTreeSelect(menus));
        resVO.setCheckedKeys(iMenuService.selectCheckedkeys(roleId));
        return R.success(resVO);
    }

    /**
     * 添加菜单
     *
     * @param menu
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @PostMapping
    public R<Boolean> addMenu(@Validated @RequestBody Menu menu) {
        if (SysConstants.NOT_UNIQUE.equals(iMenuService.checkMenuNameUnique(menu))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("新增菜单[%s]失败，菜单名称已存在", menu.getMenuName())));
        }
        menu.setCreateBy(SecurityUtils.getUsername());
        menu.setCreateTime(new Date());
        return R.result(iMenuService.save(menu));
    }

    /**
     * 修改菜单
     *
     * @param menu
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:menu:update')")
    @PutMapping
    public R<Boolean> updateMenu(@Validated @RequestBody Menu menu) {
        if (SysConstants.NOT_UNIQUE.equals(iMenuService.checkMenuNameUnique(menu))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("更新菜单[%s]失败，菜单名称已存在", menu.getMenuName())));
        }
        menu.setUpdateBy(SecurityUtils.getUsername());
        menu.setUpdateTime(new Date());
        return R.result(iMenuService.updateById(menu));
    }

    /**
     * 获取菜单信息
     *
     * @param menuId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @GetMapping("/{menuId}")
    public R<Menu> getMenu(@PathVariable Long menuId) {
        return R.success(iMenuService.getById(menuId));
    }

    /**
     * 删除菜单
     *
     * @param menuId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:menu:delete')")
    @DeleteMapping("/{menuId}")
    public R<Boolean> deleteMenu(@PathVariable Long menuId) {
        if (iMenuService.hasChildByMenuId(menuId)) {
            return R.error(ReturnCodeEnum.MENU_HAS_SUB_MENU_ERROR);
        }
        if (iMenuService.checkMenuExistRole(menuId)) {
            return R.error(ReturnCodeEnum.MENU_HAS_DISTRIBUTE_ERROR);
        }
        return R.result(iMenuService.removeById(menuId));
    }
}
