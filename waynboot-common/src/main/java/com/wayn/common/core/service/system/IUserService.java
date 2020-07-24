package com.wayn.common.core.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.system.User;

import java.util.List;

public interface IUserService extends IService<User> {

    /**
     * 查询用户列表
     *
     * @param page 分页对象
     * @param user 查询参数
     * @return 用户分页列表
     */
    IPage<User> listPage(Page<User> page, User user);


    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    void checkUserAllowed(User user);
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
    String checkPhoneUnique(User user);

    /**
     * 检查邮箱是否唯一
     * @param user 用户信息
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkEmailUnique(User user);
    /**
     * 保存用户和关联的角色信息
     * @param user 用户信息
     * @return boolean
     */
    boolean insertUserAndRole(User user);

    /**
     * 保存用户和关联的角色信息
     * @param user 用户信息
     * @return boolean
     */
    boolean updateUserAndRole(User user);

    /**
     * 查询用户列表
     * @param user 查询参数
     * @return 用户列表
     */
    List<User> list(User user);

    /**
     * 根据用户ID查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    String selectUserRoleGroup(String userName);
}
