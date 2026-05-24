package com.wayn.domain.api.promotion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.domain.api.promotion.entity.Diamond;

/**
 * 首页金刚区配置 Mapper 接口
 *
 * @author wayn
 * @since 2020-10-10
 */
public interface DiamondMapper extends BaseMapper<Diamond> {

    IPage<Diamond> selectDiamondListPage(Page<Diamond> page, Diamond diamond);
}
