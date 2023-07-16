package com.wayn.admin.api.controller.system;

import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.system.Dept;
import com.wayn.common.core.service.system.IDeptService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import com.wayn.common.util.security.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 部门管理
 *
 * @author wayn
 * @since 2020-07-21
 */
@RestController
@AllArgsConstructor
@RequestMapping("system/dept")
public class DeptController {

    private IDeptService iDeptService;

    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list")
    public R list(Dept dept) {
        List<Dept> depts = iDeptService.list(dept);
        return R.success().add("data", depts);
    }

    @PreAuthorize("@ss.hasPermi('system:dept:add')")
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
    @GetMapping("{deptId}")
    public R getDept(@PathVariable Long deptId) {
        return R.success().add("data", iDeptService.getById(deptId));
    }

    @PreAuthorize("@ss.hasPermi('system:dept:delete')")
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
