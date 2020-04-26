package com.wayn.project.system.controller;

import com.wayn.common.constant.SysConstants;
import com.wayn.common.util.R;
import com.wayn.common.util.SecurityUtils;
import com.wayn.project.system.domain.SysDept;
import com.wayn.project.system.service.IDeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Api("部门接口")
@RestController
@RequestMapping("system/dept")
public class DeptController {

    @Autowired
    private IDeptService iDeptService;

    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @ApiOperation(value = "部门列表", notes = "部门列表")
    @GetMapping("/list")
    public R list(SysDept dept) {
        List<SysDept> depts = iDeptService.list(dept);
        return R.success().add("data", depts);
    }

    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @ApiOperation(value = "保存部门", notes = "保存部门")
    @PostMapping
    public R addDept(@Validated @RequestBody SysDept dept) {
        if (SysConstants.NOT_UNIQUE.equals(iDeptService.checkDeptNameUnique(dept))) {
            return R.error("新增角色'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        dept.setCreateBy(SecurityUtils.getUsername());
        dept.setCreateTime(new Date());
        SysDept parent = iDeptService.getById(dept.getParentId());
        dept.setAncestors(parent.getAncestors() + "," + dept.getParentId());
        return R.result(iDeptService.save(dept));
    }

    @PreAuthorize("@ss.hasPermi('system:dept:update')")
    @ApiOperation(value = "更新角色", notes = "更新部门")
    @PutMapping
    public R updateDept(@Validated @RequestBody SysDept dept) {
        if (SysConstants.NOT_UNIQUE.equals(iDeptService.checkDeptNameUnique(dept))) {
            return R.error("更新角色'" + dept.getDeptName() + "'失败，部门名称已存在");
        } else if (dept.getParentId().equals(dept.getDeptId())) {
            return R.error("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        }
        dept.setUpdateBy(SecurityUtils.getUsername());
        dept.setUpdateTime(new Date());
        SysDept parent = iDeptService.getById(dept.getParentId());
        dept.setAncestors(parent.getAncestors() + "," + dept.getParentId());
        return R.result(iDeptService.updateById(dept));
    }

    /**
     * 获取部门下拉树列表
     */
    @GetMapping("/treeselect")
    public R treeselect(SysDept dept) {
        List<SysDept> depts = iDeptService.selectDeptList(dept);
        return R.success().add("deptTree", iDeptService.buildDeptTreeSelect(depts));
    }

    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @ApiOperation(value = "获取部门详细", notes = "获取部门详细")
    @GetMapping("{deptId}")
    public R getDept(@PathVariable Long deptId) {
        return R.success().add("data", iDeptService.getById(deptId));
    }

    @PreAuthorize("@ss.hasPermi('system:dept:delete')")
    @ApiOperation(value = "删除部门", notes = "删除部门")
    @DeleteMapping("{deptId}")
    public R deleteDept(@PathVariable Long deptId) {
        if (iDeptService.hasChildByDeptId(deptId)) {
            return R.error("存在下级部门,不允许删除");
        }
        if (iDeptService.checkDeptExistUser(deptId)) {
            return R.error("部门存在用户,不允许删除");
        }
        return R.result(iDeptService.removeById(deptId));
    }
}
