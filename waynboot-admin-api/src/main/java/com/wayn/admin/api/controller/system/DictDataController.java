package com.wayn.admin.api.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.system.Dict;
import com.wayn.common.core.service.system.IDictService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import com.wayn.common.util.security.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 字典值管理
 *
 * @author wayn
 * @since 2020-07-21
 */
@RestController
@AllArgsConstructor
@RequestMapping("system/dict/data")
public class DictDataController extends BaseController {

    private IDictService iDictService;

    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @GetMapping("/list")
    public R list(Dict dict) {
        Page<Dict> page = getPage();
        return R.success().add("page", iDictService.listDictDataPage(page, dict));
    }

    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @GetMapping("/selectTypeList")
    public R selectTypeList() {
        List<Dict> typeList = iDictService.list(new QueryWrapper<Dict>().eq("type", 1));
        return R.success().add("typeList", typeList);
    }

    @GetMapping("/type/{parentType}")
    public R dictType(@PathVariable String parentType) {
        List<Dict> dicts = iDictService.list(new QueryWrapper<Dict>().eq("type", 2).eq("parent_type", parentType));
        return R.success().add("data", dicts);
    }

    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @PostMapping
    public R addDict(@Validated @RequestBody Dict dict) {
        if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictNameUnique(dict))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR
                    .setMsg(String.format("新增标签名[%s]失败，标签名已存在", dict.getName())));
        } else if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictValueUnique(dict))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR
                    .setMsg(String.format("新增标签值[%s]失败，标签值已存在", dict.getValue())));
        }
        dict.setCreateBy(SecurityUtils.getUsername());
        dict.setCreateTime(new Date());
        return R.result(iDictService.save(dict));
    }

    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @PutMapping
    public R updateDict(@Validated @RequestBody Dict dict) {
        if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictNameUnique(dict))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR
                    .setMsg(String.format("更新标签名[%s]失败，标签名已存在", dict.getName())));
        } else if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictValueUnique(dict))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR
                    .setMsg(String.format("更新标签值[%s]失败，标签值已存在", dict.getValue())));
        }
        dict.setUpdateBy(SecurityUtils.getUsername());
        dict.setUpdateTime(new Date());
        return R.result(iDictService.updateById(dict));
    }

    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @GetMapping("{dictId}")
    public R getDict(@PathVariable Long dictId) {
        return R.success().add("data", iDictService.getById(dictId));
    }

    @PreAuthorize("@ss.hasPermi('system:dict:delete')")
    @DeleteMapping("{dictIds}")
    public R deleteDict(@PathVariable List<Long> dictIds) {
        return R.result(iDictService.removeByIds(dictIds));
    }

}
