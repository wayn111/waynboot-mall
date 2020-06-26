package com.wayn.admin.api.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.admin.api.domain.system.Dept;

import java.util.List;

public interface DeptMapper extends BaseMapper<Dept> {

    List<Dept> selectDeptList(Dept dept);

    List<Integer> selectDeptListByRoleId(Long roleId);
}
