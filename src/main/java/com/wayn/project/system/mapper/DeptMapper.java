package com.wayn.project.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.project.system.domain.SysDept;

import java.util.List;

public interface DeptMapper extends BaseMapper<SysDept> {

    List<SysDept> selectDeptList(SysDept dept);

    List<Integer> selectDeptListByRoleId(Long roleId);
}
