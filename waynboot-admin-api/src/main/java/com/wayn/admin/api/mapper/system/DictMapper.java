package com.wayn.admin.api.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.api.domain.system.Dict;

import java.util.List;

public interface DictMapper extends BaseMapper<Dict> {

    IPage<Dict> selectDictTypeListPage(Page<Dict> page, Dict dict);

    IPage<Dict> selectDictDataListPage(Page<Dict> page, Dict dict);

    List<Dict> selectDictTypeList(Dict dict);
}
