package com.wayn.admin.api.controller;

import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.imports.ExcelImportService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.api.domain.SysUser;
import com.wayn.admin.api.service.IRoleService;
import com.wayn.admin.api.service.IUserService;
import com.wayn.admin.framework.config.WaynConfig;
import com.wayn.admin.framework.util.SecurityUtils;
import com.wayn.common.base.BaseController;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.util.R;
import com.wayn.common.util.excel.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Api("用户接口")
@RestController
@RequestMapping("system/user")
public class UserController extends BaseController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IRoleService iRoleService;

    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @ApiOperation(value = "用户分页列表", notes = "用户分页列表")
    @GetMapping("/list")
    public R list(SysUser user) {
        Page<SysUser> page = getPage();
        return R.success().add("page", iUserService.listPage(page, user));
    }

    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @ApiOperation(value = "获取用户详细信息", notes = "获取用户详细信息")
    @GetMapping(value = {"/", "/{userId}"})
    public R getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        R success = R.success();
        success.add("roles", iRoleService.list());
        if (Objects.nonNull(userId)) {
            success.add("roleIds", iRoleService.selectRoleListByUserId(userId));
            success.add("user", iUserService.getById(userId));
        }
        return success;
    }

    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @ApiOperation(value = "添加用户", notes = "添加用户")
    @PostMapping
    public R addUser(@Validated @RequestBody SysUser user) {
        if (SysConstants.NOT_UNIQUE.equals(iUserService.checkUserNameUnique(user.getUserName()))) {
            return R.error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (SysConstants.NOT_UNIQUE.equals(iUserService.checkPhoneUnique(user))) {
            return R.error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (SysConstants.NOT_UNIQUE.equals(iUserService.checkEmailUnique(user))) {
            return R.error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setCreateBy(SecurityUtils.getUsername());
        user.setCreateTime(new Date());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return R.result(iUserService.insertUserAndRole(user));
    }

    @PreAuthorize("@ss.hasPermi('system:user:update')")
    @ApiOperation(value = "更新用户", notes = "更新用户")
    @PutMapping
    public R updateUser(@Validated @RequestBody SysUser user) {
        iUserService.checkUserAllowed(user);
        if (SysConstants.NOT_UNIQUE.equals(iUserService.checkPhoneUnique(user))) {
            return R.error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (SysConstants.NOT_UNIQUE.equals(iUserService.checkEmailUnique(user))) {
            return R.error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(SecurityUtils.getUsername());
        user.setUpdateTime(new Date());
        return R.result(iUserService.updateUserAndRole(user));
    }

    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @PutMapping("resetPwd")
    public R resetPwd(@RequestBody SysUser user) {
        iUserService.checkUserAllowed(user);
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(SecurityUtils.getUsername());
        user.setUpdateTime(new Date());
        return R.result(iUserService.updateById(user));
    }

    @PreAuthorize("@ss.hasPermi('system:user:update')")
    @ApiOperation(value = "更新用户状态", notes = "更新用户状态")
    @PutMapping("changeStatus")
    public R changeStatus(@RequestBody SysUser user) {
        iUserService.checkUserAllowed(user);
        user.setUpdateBy(SecurityUtils.getUsername());
        return R.result(iUserService.updateById(user));
    }

    @PreAuthorize("@ss.hasPermi('system:user:delete')")
    @ApiOperation(value = "删除用户", notes = "删除用户")
    @DeleteMapping("/{userIds}")
    public R deleteUser(@PathVariable List<Long> userIds) {
        return R.result(iUserService.removeByIds(userIds));
    }

    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @GetMapping("/export")
    public R export(SysUser user) {
        List<SysUser> list = iUserService.list(user);
        list.forEach(item -> item.setDeptName(item.getSysDept().getDeptName()));
        return R.success(ExcelUtil.exportExcel(list, SysUser.class, "用户数据.xls", WaynConfig.getDownloadPath()));
    }

    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @ResponseBody
    @PostMapping("/importData")
    public R importData(@RequestParam("file") MultipartFile file) throws Exception {
        InputStream inputstream = file.getInputStream();
        ImportParams params = new ImportParams();
        List<SysUser> list = new ExcelImportService().importExcelByIs(inputstream, SysUser.class, params, false).getList();
        for (SysUser user : list) {
            if (SysConstants.NOT_UNIQUE.equals(iUserService.checkUserNameUnique(user.getUserName()))) {
                return R.error("导入用户'" + user.getUserName() + "'失败，登录账号已存在");
            } else if (SysConstants.NOT_UNIQUE.equals(iUserService.checkPhoneUnique(user))) {
                return R.error("导入用户'" + user.getUserName() + "'失败，手机号码已存在");
            } else if (SysConstants.NOT_UNIQUE.equals(iUserService.checkEmailUnique(user))) {
                return R.error("导入用户'" + user.getUserName() + "'失败，邮箱账号已存在");
            }
            user.setDeptId(101L);
            user.setCreateBy(SecurityUtils.getUsername());
            user.setCreateTime(new Date());
            user.setPassword(SecurityUtils.encryptPassword(SysConstants.DEFAULT_PASSWORD));
        }
        iUserService.saveBatch(list);
        return R.success("导入用户数据成功");
    }
}
