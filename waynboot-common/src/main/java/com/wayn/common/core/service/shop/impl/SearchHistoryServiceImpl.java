package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.entity.shop.SearchHistory;
import com.wayn.common.core.mapper.shop.SearchHistoryMapper;
import com.wayn.common.core.service.shop.ISearchHistoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 搜索历史表 服务实现类
 *
 * @author wayn
 * @since 2020-09-23
 */
@Service
@AllArgsConstructor
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory> implements ISearchHistoryService {

    private SearchHistoryMapper searchHistoryMapper;

    @Override
    public List<SearchHistory> selectList(Long userId) {
        return searchHistoryMapper.selectSeachHistoryList(userId);
    }

    @Override
    public List<SearchHistory> selectHostList() {
        return searchHistoryMapper.selectHostList();
    }
}
