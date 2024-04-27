package com.wayn.common.core.mapper.shop;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.common.core.entity.shop.SearchHistory;

import java.util.List;

/**
 * 搜索历史表 Mapper 接口
 *
 * @author wayn
 * @since 2020-09-23
 */
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    List<SearchHistory> selectSeachHistoryList(Long memberId);

    List<SearchHistory> selectHostList();
}
