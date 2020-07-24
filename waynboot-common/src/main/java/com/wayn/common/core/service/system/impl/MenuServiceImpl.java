package com.wayn.common.core.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.system.Menu;
import com.wayn.common.core.domain.system.RoleMenu;
import com.wayn.common.core.domain.vo.MetaVo;
import com.wayn.common.core.domain.vo.RouterVo;
import com.wayn.common.core.domain.vo.TreeVO;
import com.wayn.common.core.mapper.system.MenuMapper;
import com.wayn.common.core.service.system.IMenuService;
import com.wayn.common.core.service.system.IRoleMenuService;
import com.wayn.common.util.security.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements IMenuService {

    @Autowired
    private MenuMapper menuMapper;

    @Autowired
    private IRoleMenuService iRoleMenuService;

    @Override
    public List<Menu> list(Menu menu) {
        return menuMapper.selectMenuList(menu);
    }

    @Override
    public List<String> selectMenuPermsByUserId(Long userId) {
        return menuMapper.selectMenuPermsByUserId(userId);
    }

    @Override
    public List<Menu> selectMenuTreeByUserId(Long userId) {
        List<Menu> menus;
        if (SecurityUtils.isAdmin(userId)) {
            menus = menuMapper.selectMenuTreeAll();
        } else {
            menus = menuMapper.selectMenuTreeByUserId(userId);
        }
        return buildMenuTreeByPid(menus, 0L);
    }

    @Override
    public List<RouterVo> buildMenus(List<Menu> menus) {
        List<RouterVo> routers = new LinkedList<>();
        for (Menu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden(1 == menu.getVisible());
            router.setName(StringUtils.capitalize(menu.getPath()));
            router.setPath(getRouterPath(menu));
            router.setComponent(StringUtils.isEmpty(menu.getComponent()) ? "Layout" : menu.getComponent());
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
            List<Menu> cMenus = menu.getChildren();
            if (!cMenus.isEmpty() && SysConstants.MENU_TYPE_M.equals(menu.getMenuType())) {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(cMenus));
            }
            routers.add(router);
        }
        return routers;
    }

    @Override
    public List<Menu> selectMenuList(Menu menu, Long userId) {
        List<Menu> menuList;
        if (SecurityUtils.isAdmin(userId)) {
            menuList = list(menu);
        } else {
            menuList = menuMapper.selectMenuListByUserId(menu, userId);
        }
        return menuList;
    }

    @Override
    public List<TreeVO> buildMenuTreeSelect(List<Menu> menus) {
        List<Menu> sysMenus = buildMenuTreeByPid(menus, 0L);
        return sysMenus.stream().map(TreeVO::new).collect(Collectors.toList());
    }

    @Override
    public List<Long> selectCheckedkeys(Long roleId) {
        List<RoleMenu> roleMenus = iRoleMenuService.list(new QueryWrapper<RoleMenu>().eq("role_id", roleId));
        List<Long> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
        if (!menuIds.isEmpty()) {
            // 去掉菜单中的父菜单
            List<Menu> menus = listByIds(menuIds);
            for (Menu menu : menus) {
                menuIds.remove(menu.getParentId());
            }
        }
        return menuIds;
    }

    @Override
    public String checkMenuNameUnique(Menu menu) {
        long menuId = Objects.isNull(menu.getMenuId()) ? -1L : menu.getMenuId();
        Menu sysMenu = getOne(new QueryWrapper<Menu>()
                .eq("menu_name", menu.getMenuName())
                .eq("parent_id", menu.getParentId()));
        if (sysMenu != null && sysMenu.getMenuId() != menuId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public boolean hasChildByMenuId(Long menuId) {
        int count = count(new QueryWrapper<Menu>().eq("parent_id", menuId));
        return count > 0;
    }

    @Override
    public boolean checkMenuExistRole(Long menuId) {
        int count = iRoleMenuService.count(new QueryWrapper<RoleMenu>().eq("menu_id", menuId));
        return count > 0;
    }

    /**
     * 构建菜单树，不包含按钮节点
     *
     * @param menus 菜单列表
     * @param pid   父级id
     * @return 菜单树
     */
    public List<Menu> buildMenuTreeByPid(List<Menu> menus, Long pid) {
        List<Menu> returnList = new ArrayList<>();
        menus.forEach(menu -> {
            if (pid.equals(menu.getParentId())) {
                // 只添加菜单类型为目录或者菜单的记录
                if (!menu.getMenuType().equals(SysConstants.MENU_TYPE_F)) {
                    menu.setChildren(buildMenuTreeByPid(menus, menu.getMenuId()));
                }
                returnList.add(menu);
            }
        });
        return returnList;
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(Menu menu) {
        String routerPath = menu.getPath();
        // 非外链并且是一级目录
        if (0 == menu.getParentId() && "1".equals(menu.getIsFrame())) {
            routerPath = "/" + menu.getPath();
        }
        return routerPath;
    }
}
