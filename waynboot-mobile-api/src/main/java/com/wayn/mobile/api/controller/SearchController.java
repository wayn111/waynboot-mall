package com.wayn.mobile.api.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.base.service.BaseElasticService;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.vo.SearchVO;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.SearchHistory;
import com.wayn.mobile.api.service.ISearchHistoryService;
import com.wayn.mobile.framework.security.util.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private BaseElasticService baseElasticService;

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
        // 查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        MatchQueryBuilder matchQuery1 = QueryBuilders.matchQuery("name", keyword);
        MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery("keyword", keyword);
        boolQueryBuilder.should(matchQuery1).should(matchPhraseQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from((int) (page.getCurrent() - 1));
        searchSourceBuilder.size((int) page.getSize());
        searchSourceBuilder.timeout(new TimeValue(10, TimeUnit.SECONDS));
        searchSourceBuilder.sort(new FieldSortBuilder("countPrice").order(SortOrder.ASC));
        List<JSONObject> list = baseElasticService.search("goods", searchSourceBuilder, JSONObject.class);
        List<Integer> goodsIdList = list.stream().filter(jsonObject -> (boolean) jsonObject.get("isOnSale")).map(jsonObject -> (Integer) jsonObject.get("id")).collect(Collectors.toList());
        if (goodsIdList.size() == 0) {
            return R.success().add("goods", Collections.emptyList());
        }
        List<Goods> goodsList = iGoodsService.list(new QueryWrapper<Goods>().in("id", goodsIdList));
        return R.success().add("goods", goodsList);
    }

}
