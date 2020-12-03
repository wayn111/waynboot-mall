package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Column;

/**
 * <p>
 * 首页栏目配置 Mapper 接口
 * </p>
 *
 * @author wayn
 * @since 2020-10-10
 */
public interface ColumnMapper extends BaseMapper<Column> {

    IPage<Column> selectColumnListPage(Page<Column> page, Column column);
}
