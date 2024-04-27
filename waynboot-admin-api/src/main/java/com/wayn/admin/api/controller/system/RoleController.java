package com.wayn.admin.api.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.framework.security.util.SecurityUtils;
import com.wayn.common.base.controller.BaseController;
import com.wayn.util.constant.SysConstants;
import com.wayn.common.core.entity.system.Role;
import com.wayn.common.core.service.system.IRoleService;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
import com.wayn.util.util.excel.ExcelUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 角色管理
 *
 * @author wayn
 * @since 2020-07-21
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("system/role")
public class RoleController extends BaseController {

    private IRoleService iRoleService;

    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/list")
    public R<IPage<Role>> list(Role role) {
        Page<Role> page = getPage();
        return R.success(iRoleService.listPage(page, role));
    }

    @PreAuthorize("@ss.hasPermi('system:role:add')")
    @PostMapping
    public R<Boolean> addRole(@Validated @RequestBody Role role) {
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
    public R<Boolean> updateRole(@Validated @RequestBody Role role) {
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
    public R<Boolean> changeStatus(@RequestBody Role role) {
        iRoleService.checkRoleAllowed(role);
        role.setUpdateBy(SecurityUtils.getUsername());
        return R.result(iRoleService.updateById(role));
    }

    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping("/{roleId}")
    public R<Role> getRole(@PathVariable Long roleId) {
        return R.success(iRoleService.getById(roleId));
    }

    @PreAuthorize("@ss.hasPermi('system:role:delete')")
    @DeleteMapping("/{roleIds}")
    public R<Boolean> deleteRole(@PathVariable List<Long> roleIds) {
        return R.result(iRoleService.deleteRoleByIds(roleIds));
    }

    @PreAuthorize("@ss.hasPermi('system:role:export')")
    @GetMapping("/export")
    public void export(Role role) {
        List<Role> list = iRoleService.list(role);
        ExcelUtil.exportExcel(response, list, Role.class, "角色数据.xlsx");
    }
}
