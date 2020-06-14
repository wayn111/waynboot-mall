package com.wayn.admin.api.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.admin.api.domain.system.SysUser;

import java.util.List;

public interface IUserService extends IService<SysUser> {

    /**
     * 查询用户列表
     *
     * @param page 分页对象
     * @param user 查询参数
     * @return 用户分页列表
     */
    IPage<SysUser> listPage(Page<SysUser> page, SysUser user);


    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    void checkUserAllowed(SysUser user);
    /**
     * 检查用户名称是否唯一
     * @param userName 用户名称
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkUserNameUnique(String userName);

    /**
     * 检查手机号是否唯一
     * @param user 用户信息
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkPhoneUnique(SysUser user);

    /**
     * 检查邮箱是否唯一
     * @param user 用户信息
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkEmailUnique(SysUser user);
    /**
     * 保存用户和关联的角色信息
     * @param user 用户信息
     * @return boolean
     */
    boolean insertUserAndRole(SysUser user);

    /**
     * 保存用户和关联的角色信息
     * @param user 用户信息
     * @return boolean
     */
    boolean updateUserAndRole(SysUser user);

    /**
     * 查询用户列表
     * @param user 查询参数
     * @return 用户列表
     */
    List<SysUser> list(SysUser user);

    /**
     * 根据用户ID查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    String selectUserRoleGroup(String userName);
}
