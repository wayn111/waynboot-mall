package com.wayn.mobile.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.mobile.api.domain.SearchHistory;

import java.util.List;

/**
 * <p>
 * 搜索历史表 Mapper 接口
 * </p>
 *
 * @author wayn
 * @since 2020-09-23
 */
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    List<SearchHistory> selectSeachHistoryList(Long memberId);

    List<SearchHistory> selectHostList();
}
