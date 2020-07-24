package com.wayn.common.core.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.system.Dept;
import com.wayn.common.core.domain.vo.TreeVO;

import java.util.List;

public interface IDeptService extends IService<Dept> {
    /**
     * 查询部门列表
     *
     * @param dept 查询参数
     * @return 部门列表
     */
    List<Dept> list(Dept dept);

    /**
     * 校验部门名称是否唯一
     *
     * @param dept 部门信息
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkDeptNameUnique(Dept dept);

    /**
     * 是否存在部门子节点
     *
     * @param deptId 部门ID
     * @return 结果
     */
    boolean hasChildByDeptId(Long deptId);

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    boolean checkDeptExistUser(Long deptId);

    /**
     * 查询部门树列表
     *
     * @param dept 选寻参数
     * @return 部门树列表
     */
    List<Dept> selectDeptList(Dept dept);


    /**
     * 构建前端所需要树结构
     *
     * @param depts 部门列表
     * @return 树结构列表
     */
    List<TreeVO> buildDeptTreeSelect(List<Dept> depts);
}
