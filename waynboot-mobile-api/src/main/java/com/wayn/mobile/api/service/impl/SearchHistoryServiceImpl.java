package com.wayn.mobile.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.mobile.api.domain.SearchHistory;
import com.wayn.mobile.api.mapper.SearchHistoryMapper;
import com.wayn.mobile.api.service.ISearchHistoryService;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 搜索历史表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-09-23
 */
@Service
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory> implements ISearchHistoryService {

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    @Override
    public List<SearchHistory> selectList() {
        Long memberId = MobileSecurityUtils.getUserId();
        return searchHistoryMapper.selectSeachHistoryList(memberId);
    }

    @Override
    public List<SearchHistory> selectHostList() {
        return searchHistoryMapper.selectHostList();
    }
}
