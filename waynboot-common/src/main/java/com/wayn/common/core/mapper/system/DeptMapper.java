package com.wayn.common.core.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.common.core.domain.system.Dept;

import java.util.List;

public interface DeptMapper extends BaseMapper<Dept> {

    List<Dept> selectDeptList(Dept dept);

    List<Integer> selectDeptListByRoleId(Long roleId);
}
