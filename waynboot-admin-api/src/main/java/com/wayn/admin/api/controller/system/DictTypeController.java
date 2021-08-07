package com.wayn.admin.api.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.config.WaynConfig;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.system.Dict;
import com.wayn.common.core.service.system.IDictService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import com.wayn.common.util.excel.ExcelUtil;
import com.wayn.common.util.security.SecurityUtils;
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
    public R list(Dict dict) {
        Page<Dict> page = getPage();
        return R.success().add("page", iDictService.listDictTypePage(page, dict));
    }


    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @ApiOperation(value = "保存字典类型", notes = "保存字典类型")
    @PostMapping
    public R addDict(@Validated @RequestBody Dict dict) {
        if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictNameUnique(dict))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("新增字典名称[%s]失败，字典名称已存在", dict.getName())));
        } else if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictValueUnique(dict))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("新增字典类型[%s]失败，字典类型已存在", dict.getValue())));
        }

        dict.setCreateBy(SecurityUtils.getUsername());
        dict.setCreateTime(new Date());
        return R.result(iDictService.save(dict));
    }

    @PreAuthorize("@ss.hasPermi('system:dict:update')")
    @ApiOperation(value = "更新字典类型", notes = "更新字典类型")
    @PutMapping
    public R updateDict(@Validated @RequestBody Dict dict) {
        if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictNameUnique(dict))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("更新字典名称[%s]失败，字典名称已存在", dict.getName())));
        } else if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictValueUnique(dict))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("更新字典类型[%s]失败，字典类型已存在", dict.getValue())));
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
    @DeleteMapping("{dictIds}")
    public R deleteDict(@PathVariable List<Long> dictIds) {
        return R.result(iDictService.deleteDictTypeById(dictIds));
    }


    @PreAuthorize("@ss.hasPermi('system:dict:export')")
    @GetMapping("/export")
    public R export(Dict dict) {
        List<Dict> list = iDictService.list(dict);
        return R.success().add("filepath", ExcelUtil.exportExcel(list, Dict.class, "字典数据.xls", WaynConfig.getDownloadPath()));
    }
}
