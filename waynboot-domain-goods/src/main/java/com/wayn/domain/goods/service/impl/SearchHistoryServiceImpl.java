package com.wayn.domain.goods.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.domain.api.goods.entity.SearchHistory;
import com.wayn.domain.api.goods.mapper.SearchHistoryMapper;
import com.wayn.domain.api.goods.service.ISearchHistoryService;
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
