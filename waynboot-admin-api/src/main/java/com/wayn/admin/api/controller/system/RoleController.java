package com.wayn.admin.api.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.config.WaynConfig;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.system.Dict;
import com.wayn.common.core.domain.system.Role;
import com.wayn.common.core.service.system.IRoleService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import com.wayn.common.util.excel.ExcelUtil;
import com.wayn.common.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("system/role")
public class RoleController extends BaseController {

    @Autowired
    private IRoleService iRoleService;


    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/list")
    public R list(Role role) {
        Page<Role> page = getPage();
        return R.success().add("page", iRoleService.listPage(page, role));
    }

    @PreAuthorize("@ss.hasPermi('system:role:add')")
    @PostMapping
    public R addRole(@Validated @RequestBody Role role) {
        if (SysConstants.NOT_UNIQUE.equals(iRoleService.checkRoleNameUnique(role))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("新增角色[%s]失败，角色名称已存在", role.getRoleName())));
        } else if (SysConstants.NOT_UNIQUE.equals(iRoleService.checkRoleKeyUnique(role))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("新增角色[%s]失败，角色权限已存在", role.getRoleName())));
        }
        role.setCreateBy(SecurityUtils.getUsername());
        role.setCreateTime(new Date());
        return R.result(iRoleService.insertRoleAndMenu(role));
    }

    @PreAuthorize("@ss.hasPermi('system:role:update')")
    @PutMapping
    public R updateRole(@Validated @RequestBody Role role) {
        iRoleService.checkRoleAllowed(role);
        if (SysConstants.NOT_UNIQUE.equals(iRoleService.checkRoleNameUnique(role))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("更新角色[%s]失败，角色名称已存在", role.getRoleName())));
        } else if (SysConstants.NOT_UNIQUE.equals(iRoleService.checkRoleKeyUnique(role))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("更新角色[%s]失败，角色权限已存在", role.getRoleName())));
        }
        role.setUpdateBy(SecurityUtils.getUsername());
        role.setUpdateTime(new Date());
        return R.result(iRoleService.updateRoleAndMenu(role));
    }

    @PreAuthorize("@ss.hasPermi('system:role:update')")
    @PutMapping("changeStatus")
    public R changeStatus(@RequestBody Role role) {
        iRoleService.checkRoleAllowed(role);
        role.setUpdateBy(SecurityUtils.getUsername());
        return R.result(iRoleService.updateById(role));
    }

    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping("/{roleId}")
    public R getRole(@PathVariable Long roleId) {
        return R.success().add("data", iRoleService.getById(roleId));
    }

    @PreAuthorize("@ss.hasPermi('system:role:delete')")
    @DeleteMapping("/{roleIds}")
    public R deleteRole(@PathVariable List<Long> roleIds) {
        return R.result(iRoleService.deleteRoleByIds(roleIds));
    }

    @PreAuthorize("@ss.hasPermi('system:role:export')")
    @GetMapping("/export")
    public void export(Role role) {
        List<Role> list = iRoleService.list(role);
        ExcelUtil.exportExcel(response, list, Role.class, "角色数据.xlsx");
    }
}
