package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Column;

/**
 * <p>
 * 首页栏目配置 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-10-10
 */
public interface IColumnService extends IService<Column> {

    /**
     * 查询栏目分页列表
     *
     * @param page   分页对象
     * @param column 查询参数
     * @return 栏目分页列表
     */
    IPage<Column> listPage(Page<Column> page, Column column);
}
