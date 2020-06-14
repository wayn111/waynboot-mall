package com.wayn.admin.api.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.admin.api.domain.system.SysDept;

import java.util.List;

public interface DeptMapper extends BaseMapper<SysDept> {

    List<SysDept> selectDeptList(SysDept dept);

    List<Integer> selectDeptListByRoleId(Long roleId);
}
