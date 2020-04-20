package com.wayn.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.util.SecurityUtils;
import com.wayn.project.system.domain.SysMenu;
import com.wayn.project.system.domain.SysRoleMenu;
import com.wayn.project.system.domain.vo.MetaVo;
import com.wayn.project.system.domain.vo.RouterVo;
import com.wayn.project.system.domain.vo.TreeVO;
import com.wayn.project.system.mapper.MenuMapper;
import com.wayn.project.system.service.IMenuService;
import com.wayn.project.system.service.IRoleMenuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, SysMenu> implements IMenuService {

    @Autowired
    private MenuMapper menuMapper;

    @Autowired
    private IRoleMenuService iRoleMenuService;

    @Override
    public IPage<SysMenu> listPage(Page<SysMenu> page, SysMenu menu) {
        return menuMapper.selectListPage(page, menu);
    }

    @Override
    public List<String> selectMenuPermsByUserId(Long userId) {
        return menuMapper.selectMenuPermsByUserId(userId);
    }

    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        List<SysMenu> menus = null;
        if (SecurityUtils.isAdmin(userId)) {
            menus = menuMapper.selectMenuTreeAll();
        } else {
            menus = menuMapper.selectMenuTreeByUserId(userId);
        }
        return buildMenuTreeByPid(menus, 0L);
    }

    @Override
    public List<RouterVo> buildMenus(List<SysMenu> menus) {
        List<RouterVo> routers = new LinkedList<>();
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden("1".equals(menu.getMenuStatus()));
            router.setName(StringUtils.capitalize(menu.getPath()));
            router.setPath(getRouterPath(menu));
            router.setComponent(StringUtils.isEmpty(menu.getComponent()) ? "Layout" : menu.getComponent());
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
            List<SysMenu> cMenus = menu.getChildren();
            if (!cMenus.isEmpty() && cMenus.size() > 0 && SysConstants.MENU_TYPE_M.equals(menu.getMenuType())) {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(cMenus));
            }
            routers.add(router);
        }
        return routers;
    }

    @Override
    public List<SysMenu> selectMenuList(SysMenu menu, Long userId) {
        List<SysMenu> menuList;
        if (SecurityUtils.isAdmin(userId)) {
            menuList = list();
        } else {
            menuList = menuMapper.selectMenuListByUserId(userId);
        }
        return menuList;
    }

    @Override
    public List<TreeVO> buildMenuTreeSelect(List<SysMenu> menus) {
        List<SysMenu> sysMenus = buildMenuTreeByPid(menus, 0L);
        return sysMenus.stream().map(TreeVO::new).collect(Collectors.toList());
    }

    @Override
    public List<Long> selectCheckedkeys(Long roleId) {
        List<SysRoleMenu> sysRoleMenus = iRoleMenuService.list(new QueryWrapper<SysRoleMenu>().eq("role_id", roleId));
        List<Long> menuIds = sysRoleMenus.stream().map(o -> o.getMenuId()).collect(Collectors.toList());
        if (!menuIds.isEmpty()) {
            // 去掉菜单中的父菜单
            List<SysMenu> sysMenus = listByIds(menuIds);
            for (SysMenu sysMenu : sysMenus) {
                if (menuIds.contains(sysMenu.getParentId())) {
                    menuIds.remove(sysMenu.getParentId());
                }
            }
        }
        return menuIds;
    }

    /**
     * 构建菜单树，不包含按钮节点
     *
     * @param menus 菜单列表
     * @param pid   父级id
     * @return 菜单树
     */
    public List<SysMenu> buildMenuTreeByPid(List<SysMenu> menus, Long pid) {
        List<SysMenu> returnList = new ArrayList<>();
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
    public String getRouterPath(SysMenu menu) {
        String routerPath = menu.getPath();
        // 非外链并且是一级目录
        if (0 == menu.getParentId() && "1".equals(menu.getIsFrame())) {
            routerPath = "/" + menu.getPath();
        }
        return routerPath;
    }
}
