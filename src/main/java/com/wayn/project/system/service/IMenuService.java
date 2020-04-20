package com.wayn.project.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.project.system.domain.SysMenu;
import com.wayn.project.system.domain.vo.RouterVo;
import com.wayn.project.system.domain.vo.TreeVO;

import java.util.List;

public interface IMenuService extends IService<SysMenu> {

    /**
     * 查询菜单列表
     *
     * @param page 分页对象
     * @param menu 查询参数
     * @return 菜单分页列表
     */
    IPage<SysMenu> listPage(Page<SysMenu> page, SysMenu menu);

    /**
     * 根据用户id查询权限
     *
     * @param userId
     * @return 权限列表
     */
    List<String> selectMenuPermsByUserId(Long userId);

    /**
     * 根据用户id查询菜单树
     *
     * @param userId
     * @return 菜单列表
     */
    List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 构建前端路由所需要的菜单
     *
     * @param menus 菜单列表
     * @return 路由列表
     */
    List<RouterVo> buildMenus(List<SysMenu> menus);

    /**
     * 根据用户id查询菜单树列表
     * @param menu 选寻参数
     * @param userId 用户id
     * @return 菜单树列表
     */
    List<SysMenu> selectMenuList(SysMenu menu, Long userId);


    /**
     * 构建前端所需要树结构
     *
     * @param menus 菜单列表
     * @return 树结构列表
     */
    List<TreeVO> buildMenuTreeSelect(List<SysMenu> menus);

    /**
     * 根据角色id 查询关联的菜单id集合
     * @param roleId 角色id
     * @return 菜单id集合
     */
    List<Long> selectCheckedkeys(Long roleId);
}
