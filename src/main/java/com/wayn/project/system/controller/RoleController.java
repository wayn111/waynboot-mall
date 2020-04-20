package com.wayn.project.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.BaseController;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.util.R;
import com.wayn.common.util.SecurityUtils;
import com.wayn.project.system.domain.SysRole;
import com.wayn.project.system.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("system/role")
public class RoleController extends BaseController {

    @Autowired
    private IRoleService iRoleService;


    @GetMapping("/list")
    public R list(SysRole role) {
        Page<SysRole> page = getPage();
        return R.success().add("page", iRoleService.listPage(page, role));
    }

    @PostMapping
    public R addRole(@Validated @RequestBody SysRole role) {
        if (SysConstants.NOT_UNIQUE.equals(iRoleService.checkRoleNameUnique(role))) {
            return R.error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (SysConstants.NOT_UNIQUE.equals(iRoleService.checkRoleKeyUnique(role))) {
            return R.error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setCreateBy(SecurityUtils.getUsername());
        role.setCreateTime(new Date());
        return R.result(iRoleService.insertRoleAndMenu(role));
    }

    @PutMapping
    public R updateRole(@Validated @RequestBody SysRole role) {
        iRoleService.checkRoleAllowed(role);
        if (SysConstants.NOT_UNIQUE.equals(iRoleService.checkRoleNameUnique(role))) {
            return R.error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (SysConstants.NOT_UNIQUE.equals(iRoleService.checkRoleKeyUnique(role))) {
            return R.error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setUpdateBy(SecurityUtils.getUsername());
        return R.result(iRoleService.updateRoleAndMenu(role));
    }

    @PutMapping("changeStatus")
    public R changeStatus(@RequestBody SysRole role) {
        iRoleService.checkRoleAllowed(role);
        role.setUpdateBy(SecurityUtils.getUsername());
        return R.result(iRoleService.updateById(role));
    }


    @GetMapping("/{roleId}")
    public R getRole(@PathVariable Long roleId) {
        return R.success().add("data", iRoleService.getById(roleId));
    }

    @DeleteMapping("/{roleIds}")
    public R getRole(@PathVariable List<Long> roleIds) {
        return R.success().add("data", iRoleService.deleteRoleByIds(roleIds));
    }
}
