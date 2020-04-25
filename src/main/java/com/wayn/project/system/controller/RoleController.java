package com.wayn.project.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.BaseController;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.util.R;
import com.wayn.common.util.SecurityUtils;
import com.wayn.common.util.excel.ExcelUtil;
import com.wayn.project.system.domain.SysRole;
import com.wayn.project.system.service.IRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@Api(value = "角色接口")
@RestController
@RequestMapping("system/role")
public class RoleController extends BaseController {

    @Autowired
    private IRoleService iRoleService;

    @ApiOperation(value = "角色列表", notes = "角色列表")
    @GetMapping("/list")
    public R list(SysRole role) {
        Page<SysRole> page = getPage();
        return R.success().add("page", iRoleService.listPage(page, role));
    }

    @ApiOperation(value = "保存角色", notes = "保存角色")
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

    @ApiOperation(value = "更新用户", notes = "更新用户")
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

    @ApiOperation(value = "更新角色状态", notes = "更新角色状态")
    @PutMapping("changeStatus")
    public R changeStatus(@RequestBody SysRole role) {
        iRoleService.checkRoleAllowed(role);
        role.setUpdateBy(SecurityUtils.getUsername());
        return R.result(iRoleService.updateById(role));
    }

    @ApiOperation("获取角色详细")
    @GetMapping("/{roleId}")
    public R getRole(@PathVariable Long roleId) {
        return R.success().add("data", iRoleService.getById(roleId));
    }

    @ApiOperation("删除角色")
    @DeleteMapping("/{roleIds}")
    public R deleteRole(@PathVariable List<Long> roleIds) {
        iRoleService.deleteRoleByIds(roleIds);
        return R.success();
    }

    @GetMapping("/export")
    public R export(SysRole role) {
        List<SysRole> list = iRoleService.list(role);
        return R.success(ExcelUtil.exportExcel(list, SysRole.class, "角色数据.xls"));
    }
}
