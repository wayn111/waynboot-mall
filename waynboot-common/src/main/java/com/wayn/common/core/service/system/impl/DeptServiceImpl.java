package com.wayn.common.core.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.system.Dept;
import com.wayn.common.core.domain.system.User;
import com.wayn.common.core.domain.vo.TreeVO;
import com.wayn.common.core.mapper.system.DeptMapper;
import com.wayn.common.core.service.system.IDeptService;
import com.wayn.common.core.service.system.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements IDeptService {

    @Autowired
    private DeptMapper deptMapper;

    @Autowired
    private IUserService iUserService;

    @Override
    public List<Dept> list(Dept dept) {
        return deptMapper.selectDeptList(dept);
    }

    @Override
    public String checkDeptNameUnique(Dept dept) {
        long deptId = Objects.isNull(dept.getDeptId()) ? -1L : dept.getDeptId();
        Dept sysDept = getOne(new QueryWrapper<Dept>()
                .eq("dept_name", dept.getDeptName())
                .eq("parent_id", dept.getParentId()));
        if (sysDept != null && sysDept.getDeptId() != deptId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public boolean hasChildByDeptId(Long deptId) {
        int count = count(new QueryWrapper<Dept>().eq("parent_id", deptId));
        return count > 0;
    }

    @Override
    public boolean checkDeptExistUser(Long deptId) {
        int count = iUserService.count(new QueryWrapper<User>().eq("dept_id", deptId));
        return count > 0;
    }

    @Override
    public List<Dept> selectDeptList(Dept dept) {
        return deptMapper.selectDeptList(dept);
    }

    @Override
    public List<TreeVO> buildDeptTreeSelect(List<Dept> depts) {
        List<Dept> sysDepts = buildDeptTreeByPid(depts, 0L);
        return sysDepts.stream().map(TreeVO::new).collect(Collectors.toList());
    }

    /**
     * 构建部门树
     *
     * @param depts 部门列表
     * @param pid   父级id
     * @return 部门树
     */
    public List<Dept> buildDeptTreeByPid(List<Dept> depts, Long pid) {
        List<Dept> returnList = new ArrayList<>();
        depts.forEach(dept -> {
            if (pid.equals(dept.getParentId())) {
                dept.setChildren(buildDeptTreeByPid(depts, dept.getDeptId()));
                returnList.add(dept);
            }
        });
        return returnList;
    }
}
