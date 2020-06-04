package com.wayn.project.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.project.system.domain.SysDict;

import java.util.List;

public interface DictMapper extends BaseMapper<SysDict> {

    IPage<SysDict> selectDictTypeListPage(Page<SysDict> page, SysDict dict);

    IPage<SysDict> selectDictDataListPage(Page<SysDict> page, SysDict dict);

    List<SysDict> selectDictTypeList(SysDict dict);
}
