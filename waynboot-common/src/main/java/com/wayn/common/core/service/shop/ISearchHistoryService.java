package com.wayn.common.core.service.shop;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.entity.shop.SearchHistory;

import java.util.List;

/**
 * 搜索历史表 服务类
 *
 * @author wayn
 * @since 2020-09-23
 */
public interface ISearchHistoryService extends IService<SearchHistory> {

    /**
     * 分页查询搜索历史
     *
     * @return 分页列表
     */
    List<SearchHistory> selectList(Long userId);

    /**
     * 查询热搜列表
     *
     * @return 热搜列表
     */
    List<SearchHistory> selectHostList();
}
