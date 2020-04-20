package com.wayn.project.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.project.system.domain.SysRole;

import java.util.List;

public interface IRoleService extends IService<SysRole> {
    /**
     * 查询角色列表
     *
     * @param page 分页对象
     * @param role 查询参数
     * @return 角色分页列表
     */
    IPage<SysRole> listPage(Page<SysRole> page, SysRole role);

    /**
     * 查询用户id查询权限
     *
     * @param userId 用户id
     * @return 权限列表
     */
    List<String> selectRoleByUserId(Long userId);

    /**
     * 校验角色名称是否唯一
     *
     * @param role 角色信息
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkRoleNameUnique(SysRole role);

    /**
     * 校验角色权限是否唯一
     *
     * @param role 角色信息
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkRoleKeyUnique(SysRole role);

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    void checkRoleAllowed(SysRole role);

    /**
     * 保存角色信息和关联菜单
     * @param role 角色信息
     * @return boolean
     */
    boolean insertRoleAndMenu(SysRole role);

    /**
     * 更新角色信息和关联菜单
     * @param role 角色信息
     * @return boolean
     */
    boolean updateRoleAndMenu(SysRole role);

    /**
     * 通过角色id删除角色
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
