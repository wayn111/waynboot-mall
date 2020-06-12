package com.wayn.admin.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.admin.api.domain.SysDept;
import com.wayn.admin.api.domain.SysUser;
import com.wayn.admin.api.domain.vo.TreeVO;
import com.wayn.admin.api.mapper.DeptMapper;
import com.wayn.admin.api.service.IDeptService;
import com.wayn.admin.api.service.IUserService;
import com.wayn.common.constant.SysConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, SysDept> implements IDeptService {

    @Autowired
    private DeptMapper deptMapper;

    @Autowired
    private IUserService iUserService;

    @Override
    public List<SysDept> list(SysDept dept) {
        return deptMapper.selectDeptList(dept);
    }

    @Override
    public String checkDeptNameUnique(SysDept dept) {
        long deptId = Objects.isNull(dept.getDeptId()) ? -1L : dept.getDeptId();
        SysDept sysDept = getOne(new QueryWrapper<SysDept>()
                .eq("dept_name", dept.getDeptName())
                .eq("parent_id", dept.getParentId()));
        if (sysDept != null && sysDept.getDeptId() != deptId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public boolean hasChildByDeptId(Long deptId) {
        int count = count(new QueryWrapper<SysDept>().eq("parent_id", deptId));
        return count > 0;
    }

    @Override
    public boolean checkDeptExistUser(Long deptId) {
        int count = iUserService.count(new QueryWrapper<SysUser>().eq("dept_id", deptId));
        return count > 0;
    }

    @Override
    public List<SysDept> selectDeptList(SysDept dept) {
        return deptMapper.selectDeptList(dept);
    }

    @Override
    public List<TreeVO> buildDeptTreeSelect(List<SysDept> depts) {
        List<SysDept> sysDepts = buildDeptTreeByPid(depts, 0L);
        return sysDepts.stream().map(TreeVO::new).collect(Collectors.toList());
    }

    /**
     * 构建部门树
     *
     * @param depts 部门列表
     * @param pid   父级id
     * @return 部门树
     */
    public List<SysDept> buildDeptTreeByPid(List<SysDept> depts, Long pid) {
        List<SysDept> returnList = new ArrayList<>();
        depts.forEach(dept -> {
            if (pid.equals(dept.getParentId())) {
                dept.setChildren(buildDeptTreeByPid(depts, dept.getDeptId()));
                returnList.add(dept);
            }
        });
        return returnList;
    }
}
