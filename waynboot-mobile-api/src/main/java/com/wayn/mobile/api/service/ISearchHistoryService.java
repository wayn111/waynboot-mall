package com.wayn.mobile.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.mobile.api.domain.SearchHistory;

import java.util.List;

/**
 * <p>
 * 搜索历史表 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-09-23
 */
public interface ISearchHistoryService extends IService<SearchHistory> {

    /**
     * 分页查询搜索历史
     * @return 分页列表
     */
    List<SearchHistory> selectList();

    /**
     * 查询热搜列表
     * @return 热搜列表
     */
    List<SearchHistory> selectHostList();
}
