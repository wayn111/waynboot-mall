package com.wayn.common.core.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.system.Dict;
import com.wayn.common.core.mapper.system.DictMapper;
import com.wayn.common.core.service.system.IDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements IDictService {

    @Autowired
    private DictMapper dictMapper;

    @Override
    public IPage<Dict> listDictTypePage(Page<Dict> page, Dict dict) {
        return dictMapper.selectDictTypeListPage(page, dict);
    }

    @Override
    public IPage<Dict> listDictDataPage(Page<Dict> page, Dict dict) {
        return dictMapper.selectDictDataListPage(page, dict);
    }

    @Override
    public String checkDictNameUnique(Dict dict) {
        long dictId = Objects.isNull(dict.getDictId()) ? -1L : dict.getDictId();
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        if (dict.getType() == 1) {
            queryWrapper.eq("name", dict.getName()).eq("type", 1);
        } else {
            queryWrapper.eq("name", dict.getName()).eq("type", 2).eq("parent_type", dict.getParentType());
        }
        Dict sysDict = getOne(queryWrapper);
        if (sysDict != null && sysDict.getDictId() != dictId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public String checkDictValueUnique(Dict dict) {
        long dictId = Objects.isNull(dict.getDictId()) ? -1L : dict.getDictId();
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        if (dict.getType() == 1) {
            queryWrapper.eq("value", dict.getValue()).eq("type", 1);
        } else {
            queryWrapper.eq("value", dict.getValue()).eq("type", 2).eq("parent_type", dict.getParentType());
        }
        Dict sysDict = getOne(queryWrapper);
        if (sysDict != null && sysDict.getDictId() != dictId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public List<Dict> list(Dict dict) {
        return dictMapper.selectDictTypeList(dict);
    }

    @Transactional
    @Override
    public boolean deleteDictTypeById(List<Long> dictIds) {
        for (Long dictId : dictIds) {
            Dict dict = getById(dictId);
            remove(new QueryWrapper<Dict>().eq("parent_type", dict.getValue()));
        }
        return removeByIds(dictIds);
    }
}
