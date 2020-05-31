package com.wayn.project.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.project.system.domain.SysDict;

public interface IDictService extends IService<SysDict> {

    /**
     * 查询字典类型列表
     *
     * @param page 分页对象
     * @param dict 查询参数
     * @return 字典类型列表
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
     * 校验字典名称是否唯一
     *
     * @param dict 字典类型
     * @return 结果
     */
    String checkDictTypeNameUnique(SysDict dict);


    /**
     * 校验字典类型称是否唯一
     *
     * @param dict 字典类型
     * @return 结果
     */
    String checkDictTypeValueUnique(SysDict dict);
}
