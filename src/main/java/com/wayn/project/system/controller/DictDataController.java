package com.wayn.project.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.BaseController;
import com.wayn.common.util.R;
import com.wayn.project.system.domain.SysDict;
import com.wayn.project.system.service.IDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Api("字典数据接口")
@RestController
@RequestMapping("system/dict/data")
public class DictDataController extends BaseController {

    @Autowired
    private IDictService iDictService;


    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @ApiOperation(value = "字典数据列表", notes = "字典数据列表")
    @GetMapping("/list")
    public R list(SysDict dict) {
        Page<SysDict> page = getPage();
        return R.success().add("page", iDictService.listDictDataPage(page, dict));
    }

    @GetMapping("/type/{parentType}")
    public R dictType(@PathVariable String parentType) {
        List<SysDict> dicts = iDictService.list(new QueryWrapper<SysDict>().eq("type", 2).eq("parent_type", parentType));
        return R.success().add("data", dicts);
    }
}
