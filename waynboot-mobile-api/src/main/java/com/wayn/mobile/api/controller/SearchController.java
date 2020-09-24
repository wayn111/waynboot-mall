package com.wayn.mobile.api.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.BaseController;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.vo.SearchVO;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.SearchHistory;
import com.wayn.mobile.api.service.ISearchHistoryService;
import com.wayn.mobile.framework.security.util.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 搜索历史表 前端控制器
 * </p>
 *
 * @author wayn
 * @since 2020-09-23
 */
@RestController
@RequestMapping("search")
public class SearchController extends BaseController {

    @Autowired
    private IGoodsService iGoodsService;

    @Autowired
    private ISearchHistoryService iSearchHistoryService;

    @GetMapping("result")
    public R list(SearchVO searchVO) {
        Long memberId = SecurityUtils.getUserId();
        String keyword = searchVO.getKeyword();
        Integer categoryId = searchVO.getCategoryId();
        Boolean isHot = searchVO.getIsHot();
        Boolean isNew = searchVO.getIsNew();
        if (memberId != null && StringUtils.isNotEmpty(keyword)) {
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setCreateTime(LocalDateTime.now());
            searchHistory.setUserId(memberId);
            searchHistory.setKeyword(keyword);
            iSearchHistoryService.save(searchHistory);
        }
        Page<SearchVO> page = getPage();
        List<Goods> goods = iGoodsService.searchResult(page, searchVO);
        return R.success().add("goods", goods);
    }

}
