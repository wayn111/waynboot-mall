package com.wayn.admin.api.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.framework.security.util.SecurityUtils;
import com.wayn.common.base.controller.BaseController;
import com.wayn.util.constant.SysConstants;
import com.wayn.common.core.entity.system.Dict;
import com.wayn.common.core.service.system.IDictService;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
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

    /**
     * 分页列表
     *
     * @param dict
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @GetMapping("/list")
    public R<IPage<Dict>> list(Dict dict) {
        Page<Dict> page = getPage();
        return R.success(iDictService.listDictDataPage(page, dict));
    }

    /**
     * 查询字典类型列表
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @GetMapping("/selectTypeList")
    public R<List<Dict>> selectTypeList() {
        List<Dict> typeList = iDictService.list(new QueryWrapper<Dict>().eq("type", 1));
        return R.success(typeList);
    }

    /**
     * 根据字典类型获取字典值列表
     * @param parentType
     * @return
     */
    @GetMapping("/type/{parentType}")
    public R<List<Dict>> dictType(@PathVariable String parentType) {
        List<Dict> dicts = iDictService.list(new QueryWrapper<Dict>().eq("type", 2).eq("parent_type", parentType));
        return R.success(dicts);
    }

    /**
     * 添加字典值
     * @param dict
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @PostMapping
    public R<Boolean> addDict(@Validated @RequestBody Dict dict) {
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

    /**
     * 修改字典值
     * @param dict
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @PutMapping
    public R<Boolean> updateDict(@Validated @RequestBody Dict dict) {
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

    /**
     * 获取字典值
     * @param dictId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @GetMapping("{dictId}")
    public R<Dict> getDict(@PathVariable Long dictId) {
        return R.success(iDictService.getById(dictId));
    }

    /**
     * 删除字典值
     * @param dictIds
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dict:delete')")
    @DeleteMapping("{dictIds}")
    public R<Boolean> deleteDict(@PathVariable List<Long> dictIds) {
        return R.result(iDictService.removeByIds(dictIds));
    }

}
