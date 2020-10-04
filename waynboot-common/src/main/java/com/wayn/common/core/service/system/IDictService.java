package com.wayn.common.core.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.system.Dict;

import java.util.List;

public interface IDictService extends IService<Dict> {

    /**
     * 查询字典类型分页列表
     *
     * @param page 分页对象
     * @param dict 查询参数
     * @return 字典类型分页列表
     */
    IPage<Dict> listDictTypePage(Page<Dict> page, Dict dict);

    /**
     * 查询字典数据列表
     *
     * @param page 分页对象
     * @param dict 查询参数
     * @return 字典数据列表
     */
    IPage<Dict> listDictDataPage(Page<Dict> page, Dict dict);

    /**
     * 校验字典name是否唯一
     *
     * @param dict 字典类型
     * @return 结果
     */
    String checkDictNameUnique(Dict dict);


    /**
     * 校验字典value是否唯一
     *
     * @param dict 字典类型
     * @return 结果
     */
    String checkDictValueUnique(Dict dict);

    /**
     * 查询字典类型列表
     *
     * @param dict 查询参数
     * @return 字典类型列表
     */
    List<Dict> list(Dict dict);

    /**
     * 删除字典类型以及子数据
     * @param dictIds 字典id集合
     * @return boolean
     */
    boolean deleteDictTypeById(List<Long> dictIds);
}
