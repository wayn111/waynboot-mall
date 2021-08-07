package com.wayn.admin.api.controller.system;

import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.system.Dept;
import com.wayn.common.core.service.system.IDeptService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import com.wayn.common.util.security.SecurityUtils;
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
    public R list(Dept dept) {
        List<Dept> depts = iDeptService.list(dept);
        return R.success().add("data", depts);
    }

    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @ApiOperation(value = "保存部门", notes = "保存部门")
    @PostMapping
    public R addDept(@Validated @RequestBody Dept dept) {
        if (SysConstants.NOT_UNIQUE.equals(iDeptService.checkDeptNameUnique(dept))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR
                    .setMsg(String.format("新增部门[%s]失败，部门名称已存在", dept.getDeptName())));
        }
        dept.setCreateBy(SecurityUtils.getUsername());
        dept.setCreateTime(new Date());
        Dept parent = iDeptService.getById(dept.getParentId());
        dept.setAncestors(parent.getAncestors() + "," + dept.getParentId());
        return R.result(iDeptService.save(dept));
    }

    @PreAuthorize("@ss.hasPermi('system:dept:update')")
    @ApiOperation(value = "更新部门", notes = "更新部门")
    @PutMapping
    public R updateDept(@Validated @RequestBody Dept dept) {
        if (SysConstants.NOT_UNIQUE.equals(iDeptService.checkDeptNameUnique(dept))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR
                    .setMsg(String.format("更新部门[%s]失败，部门名称已存在", dept.getDeptName())));
        } else if (dept.getParentId().equals(dept.getDeptId())) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR
                    .setMsg(String.format("修改部门[%s]失败，部门名称已存在", dept.getDeptName())));
        }
        dept.setUpdateBy(SecurityUtils.getUsername());
        dept.setUpdateTime(new Date());
        Long topParentId = 0L;
        if (!topParentId.equals(dept.getParentId())) {
            Dept parent = iDeptService.getById(dept.getParentId());
            dept.setAncestors(parent.getAncestors() + "," + dept.getParentId());
        }
        return R.result(iDeptService.updateById(dept));
    }

    /**
     * 获取部门下拉树列表
     */
    @GetMapping("/treeselect")
    public R treeselect(Dept dept) {
        List<Dept> depts = iDeptService.selectDeptList(dept);
        return R.success().add("deptTree", iDeptService.buildDeptTreeSelect(depts));
    }

    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @ApiOperation(value = "获取部门详细信息", notes = "获取部门详细信息")
    @GetMapping("{deptId}")
    public R getDept(@PathVariable Long deptId) {
        return R.success().add("data", iDeptService.getById(deptId));
    }

    @PreAuthorize("@ss.hasPermi('system:dept:delete')")
    @ApiOperation(value = "删除部门", notes = "删除部门")
    @DeleteMapping("{deptId}")
    public R deleteDept(@PathVariable Long deptId) {
        if (iDeptService.hasChildByDeptId(deptId)) {
            return R.error(ReturnCodeEnum.DEPT_HAS_SUB_DEPT_ERROR);
        }
        if (iDeptService.checkDeptExistUser(deptId)) {
            return R.error(ReturnCodeEnum.DEPT_HAS_USER_ERROR);
        }
        return R.result(iDeptService.removeById(deptId));
    }
}
