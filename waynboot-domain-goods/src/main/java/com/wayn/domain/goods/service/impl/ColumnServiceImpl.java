package com.wayn.domain.goods.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.domain.api.goods.entity.Column;
import com.wayn.domain.api.goods.mapper.ColumnMapper;
import com.wayn.domain.api.goods.service.IColumnService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 首页栏目配置 服务实现类
 *
 * @author wayn
 * @since 2020-10-10
 */
@Service
@AllArgsConstructor
public class ColumnServiceImpl extends ServiceImpl<ColumnMapper, Column> implements IColumnService {

    private ColumnMapper columnMapper;

    @Override
    public IPage<Column> listPage(Page<Column> page, Column column) {
        return columnMapper.selectColumnListPage(page, column);
    }
}
