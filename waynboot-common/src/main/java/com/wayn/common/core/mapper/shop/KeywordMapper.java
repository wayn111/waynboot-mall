package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Keyword;

/**
 * 关键字表 Mapper 接口
 *
 * @author wayn
 * @since 2020-11-02
 */
public interface KeywordMapper extends BaseMapper<Keyword> {

    IPage<Keyword> selectKeywordListPage(Page<Keyword> page, Keyword keyword);
}
