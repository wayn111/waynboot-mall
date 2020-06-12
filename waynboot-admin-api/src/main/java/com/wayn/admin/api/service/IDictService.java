package com.wayn.admin.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.admin.api.domain.SysDict;

import java.util.List;

public interface IDictService extends IService<SysDict> {

    /**
     * 查询字典类型分页列表
     *
     * @param page 分页对象
     * @param dict 查询参数
     * @return 字典类型分页列表
     */
    IPage<SysDict> listDictTypePage(Page<SysDict> page, SysDict dict);

    /**
     * 查询字典数据列表
     *
     * @param page 分页对象
     * @param dict 查询参数
     * @return 字典数据列表
     */
    IPage<SysDict> listDictDataPage(Page<SysDict> page, SysDict dict);

    /**
     * 校验字典name是否唯一
     *
     * @param dict 字典类型
     * @return 结果
     */
    String checkDictNameUnique(SysDict dict);


    /**
     * 校验字典value是否唯一
     *
     * @param dict 字典类型
     * @return 结果
     */
    String checkDictValueUnique(SysDict dict);

    /**
     * 查询字典类型列表
     *
     * @param dict 查询参数
     * @return 字典类型列表
     */
    List<SysDict> list(SysDict dict);

    /**
     * 删除字典类型以及子数据
     * @param dictId 字典id
     * @return boolean
     */
    boolean deleteDictTypeById(Long dictId);
}
