package com.wayn.admin.api.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.framework.security.util.SecurityUtils;
import com.wayn.common.base.controller.BaseController;
import com.wayn.util.constant.SysConstants;
import com.wayn.common.core.entity.system.Dict;
import com.wayn.common.core.service.system.IDictService;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
import com.wayn.util.util.excel.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 字典管理
 *
 * @author wayn
 * @since 2020-07-21
 */
@RestController
@AllArgsConstructor
@RequestMapping("system/dict/type")
public class DictTypeController extends BaseController {

    private IDictService iDictService;

    /**
     * 分页列表
     * @param dict
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @GetMapping("/list")
    public R<IPage<Dict>> list(Dict dict) {
        Page<Dict> page = getPage();
        return R.success(iDictService.listDictTypePage(page, dict));
    }

    /**
     * 添加字典
     * @param dict
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @PostMapping
    public R<Boolean> addDict(@Validated @RequestBody Dict dict) {
        if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictNameUnique(dict))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("新增字典名称[%s]失败，字典名称已存在", dict.getName())));
        } else if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictValueUnique(dict))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("新增字典类型[%s]失败，字典类型已存在", dict.getValue())));
        }

        dict.setCreateBy(SecurityUtils.getUsername());
        dict.setCreateTime(new Date());
        return R.result(iDictService.save(dict));
    }

    /**
     * 修改字典
     * @param dict
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dict:update')")
    @PutMapping
    public R<Boolean> updateDict(@Validated @RequestBody Dict dict) {
        if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictNameUnique(dict))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("更新字典名称[%s]失败，字典名称已存在", dict.getName())));
        } else if (SysConstants.NOT_UNIQUE.equals(iDictService.checkDictValueUnique(dict))) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg(String.format("更新字典类型[%s]失败，字典类型已存在", dict.getValue())));
        }
        dict.setUpdateBy(SecurityUtils.getUsername());
        dict.setUpdateTime(new Date());
        return R.result(iDictService.updateById(dict));
    }

    /**
     * 获取字典
     * @param dictId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @GetMapping("{dictId}")
    public R<Dict> getDict(@PathVariable Long dictId) {
        return R.success(iDictService.getById(dictId));
    }

    /**
     * 删除字典
     * @param dictIds
     * @return
     */
    @PreAuthorize("@ss.hasPermi('system:dict:delete')")
    @DeleteMapping("{dictIds}")
    public R<Boolean> deleteDict(@PathVariable List<Long> dictIds) {
        return R.result(iDictService.deleteDictTypeById(dictIds));
    }

    /**
     * 导出字典
     * @param dict
     * @param response
     */
    @PreAuthorize("@ss.hasPermi('system:dict:export')")
    @GetMapping("/export")
    public void export(Dict dict, HttpServletResponse response) {
        List<Dict> list = iDictService.list(dict);
        ExcelUtil.exportExcel(response, list, Dict.class, "字典数据.xlsx");
    }
}
