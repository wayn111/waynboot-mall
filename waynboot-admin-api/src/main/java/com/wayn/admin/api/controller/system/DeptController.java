package com.wayn.admin.api.controller.system;

import com.wayn.admin.framework.security.util.SecurityUtils;
import com.wayn.common.core.entity.system.Dept;
import com.wayn.common.core.service.system.IDeptService;
import com.wayn.common.core.vo.TreeVO;
import com.wayn.util.constant.SysConstants;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 后台部门管理
 *
 * @author wayn
 * @since 2020-07-21
 */
@RestController
@AllArgsConstructor
@RequestMapping("system/dept")
public class DeptController {

    private IDeptService iDeptService;

    /**
     * 部门列表
     *
     * @param dept
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list")
    public R<List<Dept>> list(Dept dept) {
        List<Dept> depts = iDeptService.list(dept);
        return R.success(depts);
    }

    /**
     * 添加部门
     *
     * @param dept
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @PostMapping
    public R<Boolean> addDept(@Validated @RequestBody Dept dept) {
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

    /**
     * 修改部门
     *
     * @param dept
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dept:update')")
    @PutMapping
    public R<Boolean> updateDept(@Validated @RequestBody Dept dept) {
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
    public R<List<TreeVO>> treeselect(Dept dept) {
        List<Dept> depts = iDeptService.selectDeptList(dept);
        return R.success(iDeptService.buildDeptTreeSelect(depts));
    }

    /**
     * 获取部门信息
     *
     * @param deptId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @GetMapping("{deptId}")
    public R<Dept> getDept(@PathVariable Long deptId) {
        return R.success(iDeptService.getById(deptId));
    }

    /**
     * 删除部门
     *
     * @param deptId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dept:delete')")
    @DeleteMapping("{deptId}")
    public R<Boolean> deleteDept(@PathVariable Long deptId) {
        if (iDeptService.hasChildByDeptId(deptId)) {
            return R.error(ReturnCodeEnum.DEPT_HAS_SUB_DEPT_ERROR);
        }
        if (iDeptService.checkDeptExistUser(deptId)) {
            return R.error(ReturnCodeEnum.DEPT_HAS_USER_ERROR);
        }
        return R.result(iDeptService.removeById(deptId));
    }
}
