package com.wayn.admin.api.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.system.User;
import com.wayn.common.core.service.system.IRoleService;
import com.wayn.common.core.service.system.IUserService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import com.wayn.common.util.excel.ExcelUtil;
import com.wayn.common.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("system/user")
public class UserController extends BaseController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IRoleService iRoleService;

    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public R list(User user) {
        Page<User> page = getPage();
        return R.success().add("page", iUserService.listPage(page, user));
    }

    @PreAuthorize("@ss.hasPermi('system:user:query')")
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
    @PostMapping
    public R addUser(@Validated @RequestBody User user) {
        if (SysConstants.NOT_UNIQUE.equals(iUserService.checkUserNameUnique(user.getUserName()))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("导入用户[%s]失败，登录账号已存在", user.getUserName())));
        } else if (SysConstants.NOT_UNIQUE.equals(iUserService.checkPhoneUnique(user))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("导入用户[%s]失败，手机号码已存在", user.getUserName())));
        } else if (SysConstants.NOT_UNIQUE.equals(iUserService.checkEmailUnique(user))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("导入用户[%s]失败，邮箱账号已存在", user.getUserName())));
        }
        user.setAvatar("http://cdn.wayn.xin/80af3951523e76f4818ac7fff1223808.jpeg");
        user.setCreateBy(SecurityUtils.getUsername());
        user.setCreateTime(new Date());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return R.result(iUserService.insertUserAndRole(user));
    }

    @PreAuthorize("@ss.hasPermi('system:user:update')")
    @PutMapping
    public R updateUser(@Validated @RequestBody User user) {
        iUserService.checkUserAllowed(user);
        if (SysConstants.NOT_UNIQUE.equals(iUserService.checkPhoneUnique(user))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("导入用户[%s]失败，手机号码已存在", user.getUserName())));
        } else if (SysConstants.NOT_UNIQUE.equals(iUserService.checkEmailUnique(user))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("导入用户[%s]失败，邮箱账号已存在", user.getUserName())));
        }
        user.setUpdateBy(SecurityUtils.getUsername());
        user.setUpdateTime(new Date());
        return R.result(iUserService.updateUserAndRole(user));
    }

    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @PutMapping("resetPwd")
    public R resetPwd(@RequestBody User user) {
        iUserService.checkUserAllowed(user);
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(SecurityUtils.getUsername());
        user.setUpdateTime(new Date());
        return R.result(iUserService.updateById(user));
    }

    @PreAuthorize("@ss.hasPermi('system:user:update')")
    @PutMapping("changeStatus")
    public R changeStatus(@RequestBody User user) {
        iUserService.checkUserAllowed(user);
        user.setUpdateBy(SecurityUtils.getUsername());
        return R.result(iUserService.updateById(user));
    }

    @PreAuthorize("@ss.hasPermi('system:user:delete')")
    @DeleteMapping("/{userIds}")
    public R deleteUser(@PathVariable List<Long> userIds) {
        return R.result(iUserService.removeByIds(userIds));
    }

    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @GetMapping("/export")
    public void export(User user, HttpServletResponse response) {
        List<User> list = iUserService.list(user);
        list.forEach(item -> item.setDeptName(item.getDept().getDeptName()));
        ExcelUtil.exportExcel(response, list, User.class, "用户数据.xlsx");
    }

    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @ResponseBody
    @PostMapping("/importData")
    public R importData(@RequestParam("file") MultipartFile file) {
        return iUserService.importUser(file);
    }

}
