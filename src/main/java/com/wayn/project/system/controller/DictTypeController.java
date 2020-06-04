package com.wayn.project.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.BaseController;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.util.R;
import com.wayn.common.util.SecurityUtils;
import com.wayn.common.util.excel.ExcelUtil;
import com.wayn.project.system.domain.SysDict;
import com.wayn.project.system.service.IDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;


@Api("字典类型接口")
@RestController
@RequestMapping("system/dict/type")
public class DictTypeController extends BaseController {

    @Autowired
    private IDictService iDictService;


    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @ApiOperation(value = "字典类型分页", notes = "字典类型分页")
    @GetMapping("/list")
    public R list(SysDict dict) {
        Page<SysDict> page = getPage();
        return R.success().add("page", iDictService.listDictTypePage(page, dict));
    }


    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @ApiOperation(value = "保存字典类型", notes = "保存字典类型")
    @PostMapping
    public R addDict(@Validated @RequestBody SysDict dict) {
        if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictNameUnique(dict))) {
            return R.error("新增字典名称'" + dict.getName() + "'失败，字典名称已存在");
        } else if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictValueUnique(dict))) {
            return R.error("新增字典类型'" + dict.getValue() + "'失败，字典类型已存在");
        }
        dict.setCreateBy(SecurityUtils.getUsername());
        dict.setCreateTime(new Date());
        return R.result(iDictService.save(dict));
    }

    @PreAuthorize("@ss.hasPermi('system:dict:update')")
    @ApiOperation(value = "更新字典类型", notes = "更新字典类型")
    @PutMapping
    public R updateDict(@Validated @RequestBody SysDict dict) {
        if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictNameUnique(dict))) {
            return R.error("更新字典名称'" + dict.getName() + "'失败，字典名称已存在");
        } else if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictValueUnique(dict))) {
            return R.error("更新字典类型'" + dict.getValue() + "'失败，字典类型已存在");
        }
        dict.setUpdateBy(SecurityUtils.getUsername());
        dict.setUpdateTime(new Date());
        return R.result(iDictService.updateById(dict));
    }

    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @ApiOperation(value = "获取字典类型详细", notes = "获取字典类型详细")
    @GetMapping("{dictId}")
    public R getDict(@PathVariable Long dictId) {
        return R.success().add("data", iDictService.getById(dictId));
    }

    @PreAuthorize("@ss.hasPermi('system:dict:delete')")
    @ApiOperation(value = "删除字典类型", notes = "删除字典类型")
    @DeleteMapping("{dictId}")
    public R deleteDict(@PathVariable Long dictId) {
//        return R.result(iDictService.removeById(dictId));
        return R.success();
    }


    @PreAuthorize("@ss.hasPermi('system:dict:export')")
    @GetMapping("/export")
    public R export(SysDict dict) {
        List<SysDict> list = iDictService.list(dict);
        return R.success(ExcelUtil.exportExcel(list, SysDict.class, "字典数据.xls"));
    }
}
