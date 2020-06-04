package com.wayn.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.constant.SysConstants;
import com.wayn.project.system.domain.SysDict;
import com.wayn.project.system.mapper.DictMapper;
import com.wayn.project.system.service.IDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, SysDict> implements IDictService {

    @Autowired
    private DictMapper dictMapper;

    @Override
    public IPage<SysDict> listDictTypePage(Page<SysDict> page, SysDict dict) {
        return dictMapper.selectDictTypeListPage(page, dict);
    }

    @Override
    public IPage<SysDict> listDictDataPage(Page<SysDict> page, SysDict dict) {
        return dictMapper.selectDictDataListPage(page, dict);
    }

    @Override
    public String checkDictNameUnique(SysDict dict) {
        long dictId = Objects.isNull(dict.getDictId()) ? -1L : dict.getDictId();
        QueryWrapper<SysDict> queryWrapper = new QueryWrapper<>();
        if (dict.getType() == 1) {
            queryWrapper.eq("name", dict.getName()).eq("type", 1);
        } else {
            queryWrapper.eq("name", dict.getName()).eq("type", 2).eq("parent_type", dict.getParentType());
        }
        SysDict sysDict = getOne(queryWrapper);
        if (sysDict != null && sysDict.getDictId() != dictId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public String checkDictValueUnique(SysDict dict) {
        long dictId = Objects.isNull(dict.getDictId()) ? -1L : dict.getDictId();
        QueryWrapper<SysDict> queryWrapper = new QueryWrapper<>();
        if (dict.getType() == 1) {
            queryWrapper.eq("value", dict.getValue()).eq("type", 1);
        } else {
            queryWrapper.eq("value", dict.getValue()).eq("type", 2).eq("parent_type", dict.getParentType());
        }
        SysDict sysDict = getOne(queryWrapper);
        if (sysDict != null && sysDict.getDictId() != dictId) {
            return SysConstants.NOT_UNIQUE;
        }
        return SysConstants.UNIQUE;
    }

    @Override
    public List<SysDict> list(SysDict dict) {
        return dictMapper.selectDictTypeList(dict);
    }
}
