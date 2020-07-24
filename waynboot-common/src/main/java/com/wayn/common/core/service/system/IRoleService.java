package com.wayn.common.core.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.system.Role;

import java.util.List;

public interface IRoleService extends IService<Role> {
    /**
     * 查询角色列表
     *
     * @param page 分页对象
     * @param role 查询参数
     * @return 角色分页列表
     */
    IPage<Role> listPage(Page<Role> page, Role role);

    /**
     * 查询角色列表
     * @param role 查询参数
     * @return 角色列表
     */
    List<Role> list(Role role);

    /**
     * 根据用户id查询权限
     *
     * @param userId 用户id
     * @return 权限列表
     */
    List<String> selectRoleByUserId(Long userId);

    /**
     * 根据用户id获取角色选择框列表
     *
     * @param userId 用户id
     * @return 选中角色id列表
     */
    public List<Integer> selectRoleListByUserId(Long userId);

    /**
     * 校验角色名称是否唯一
     *
     * @param role 角色信息
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkRoleNameUnique(Role role);

    /**
     * 校验角色权限是否唯一
     *
     * @param role 角色信息
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkRoleKeyUnique(Role role);

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    void checkRoleAllowed(Role role);

    /**
     * 保存角色信息和关联菜单
     *
     * @param role 角色信息
     * @return boolean
     */
    boolean insertRoleAndMenu(Role role);

    /**
     * 更新角色信息和关联菜单
     *
     * @param role 角色信息
     * @return boolean
     */
    boolean updateRoleAndMenu(Role role);

    /**
     * 通过角色id删除角色
     *
     * @param roleIds
     * @return boolean
     */
    boolean deleteRoleByIds(List<Long> roleIds);

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色id
     * @return int
     */
    int countUserRoleByRoleId(Long roleId);
}
