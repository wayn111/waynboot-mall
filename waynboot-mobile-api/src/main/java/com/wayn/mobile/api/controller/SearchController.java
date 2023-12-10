package com.wayn.mobile.api.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.shop.Keyword;
import com.wayn.common.core.domain.vo.SearchVO;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IKeywordService;
import com.wayn.common.util.R;
import com.wayn.data.elastic.manager.ElasticDocument;
import com.wayn.mobile.api.domain.SearchHistory;
import com.wayn.mobile.api.service.ISearchHistoryService;
import com.wayn.mobile.framework.manager.thread.AsyncManager;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 搜索历史表 前端控制器
 *
 * @author wayn
 * @since 2020-09-23
 */
@RestController
@AllArgsConstructor
@RequestMapping("search")
public class SearchController extends BaseController {

    private IGoodsService iGoodsService;

    private ISearchHistoryService iSearchHistoryService;

    private IKeywordService iKeywordService;

    private ElasticDocument elasticDocument;

    /**
     * 商城搜索建议
     * @param searchVO
     * @return
     * @throws IOException
     */
    @GetMapping("sugguest")
    public R sugguest(SearchVO searchVO) throws IOException {
        String keyword = searchVO.getKeyword();
        String suggestField = "name.py";
        String suggestName = "my-suggest";
        SuggestionBuilder<CompletionSuggestionBuilder> termSuggestionBuilder = SuggestBuilders.completionSuggestion(suggestField)
                .prefix(keyword)
                .skipDuplicates(true)
                .size(10);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion(suggestName, termSuggestionBuilder);
        List<String> list = elasticDocument.searchSuggest("goods", suggestName, suggestBuilder);
        return R.success().add("suggest", list);
    }

    /**
     * 商城搜索结果
     * @param searchVO
     * @return
     * @throws IOException
     */
    @GetMapping("result")
    public R result(SearchVO searchVO) throws IOException {
        // 获取筛选、排序条件
        Long memberId = MobileSecurityUtils.getUserId();
        String keyword = searchVO.getKeyword();
        Boolean filterNew = searchVO.getFilterNew();
        Boolean filterHot = searchVO.getFilterHot();
        Boolean isNew = searchVO.getIsNew();
        Boolean isHot = searchVO.getIsHot();
        Boolean isPrice = searchVO.getIsPrice();
        Boolean isSales = searchVO.getIsSales();
        String orderBy = searchVO.getOrderBy();

        Page<SearchVO> page = getPage();
        // 查询包含关键字、已上架商品
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        MatchQueryBuilder matchFiler = QueryBuilders.matchQuery("isOnSale", true);
        MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("name", keyword);
        MatchQueryBuilder pymatchQuery = QueryBuilders.matchQuery("pyname", keyword);
        MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery("keyword", keyword);
        boolQueryBuilder.filter(matchFiler).should(matchQuery)
                .should(pymatchQuery)
                .should(matchPhraseQueryBuilder)
                .minimumShouldMatch(1);
        searchSourceBuilder.timeout(new TimeValue(10, TimeUnit.SECONDS));
        // 按是否新品排序
        if (isNew) {
            searchSourceBuilder.sort(new FieldSortBuilder("isNew").order(SortOrder.DESC));
        }
        // 按是否热品排序
        if (isHot) {
            searchSourceBuilder.sort(new FieldSortBuilder("isHot").order(SortOrder.DESC));
        }
        // 按价格高低排序
        if (isPrice) {
            searchSourceBuilder.sort(new FieldSortBuilder("retailPrice").order("asc".equals(orderBy) ? SortOrder.ASC : SortOrder.DESC));
        }
        // 按销量排序
        if (isSales) {
            searchSourceBuilder.sort(new FieldSortBuilder("sales").order(SortOrder.DESC));
        }
        // 筛选新品
        if (filterNew) {
            MatchQueryBuilder filterQuery = QueryBuilders.matchQuery("isNew", true);
            boolQueryBuilder.filter(filterQuery);
        }
        // 筛选热品
        if (filterHot) {
            MatchQueryBuilder filterQuery = QueryBuilders.matchQuery("isHot", true);
            boolQueryBuilder.filter(filterQuery);
        }

        // 组装Elasticsearch查询条件
        searchSourceBuilder.query(boolQueryBuilder);
        // Elasticsearch分页相关
        searchSourceBuilder.from((int) (page.getCurrent() - 1) * (int) page.getSize());
        searchSourceBuilder.size((int) page.getSize());
        // 执行Elasticsearch查询
        List<JSONObject> list = elasticDocument.searchResult("goods", searchSourceBuilder, JSONObject.class);
        List<Integer> goodsIdList = list.stream().map(jsonObject -> (Integer) jsonObject.get("id")).collect(Collectors.toList());
        if (goodsIdList.isEmpty()) {
            return R.success().add("goods", Collections.emptyList());
        }
        // 根据Elasticsearch中返回商品ID查询商品详情并保持es中的排序
        List<Goods> goodsList = iGoodsService.searchResult(goodsIdList);
        Map<Integer, Goods> goodsMap = goodsList.stream().collect(Collectors.toMap(goods -> Math.toIntExact(goods.getId()), o -> o));
        List<Goods> returnGoodsList = new ArrayList<>(goodsList.size());
        for (Integer goodsId : goodsIdList) {
            returnGoodsList.add(goodsMap.get(goodsId));
        }
        if (CollectionUtils.isNotEmpty(goodsList)) {
            AsyncManager.me().execute(new TimerTask() {
                @Override
                public void run() {
                    SearchHistory searchHistory = new SearchHistory();
                    if (memberId != null && StringUtils.isNotEmpty(keyword)) {
                        searchHistory.setCreateTime(LocalDateTime.now());
                        searchHistory.setUserId(memberId);
                        searchHistory.setKeyword(keyword);
                        searchHistory.setHasGoods(true);
                        iSearchHistoryService.save(searchHistory);
                    }
                }
            });
        }
        return R.success().add("goods", returnGoodsList);
    }

    /**
     * 热门搜索
     * @return R
     */
    @GetMapping("hotKeywords")
    public R hotKeywords() {
        // 查询配置了热门搜索展示的关键词
        List<Keyword> hotKeywords = iKeywordService.list(new QueryWrapper<Keyword>().eq("is_hot", true).orderByAsc("sort"));
        List<String> hotStrings = hotKeywords.stream().map(Keyword::getKeyword).collect(Collectors.toList());

        // 查询配置了默认搜索展示的关键词，如果有多个配置了默认搜索，就按照排序值从小到大取第一个
        List<Keyword> defaultKeyword = iKeywordService.list(new QueryWrapper<Keyword>().eq("is_default", true).orderByAsc("sort"));
        List<String> defaultStrings = defaultKeyword.stream().map(Keyword::getKeyword).collect(Collectors.toList());
        R r = R.success();
        if (CollectionUtils.isNotEmpty(hotStrings)) {
            r.add("data", hotStrings);
        }
        r.add("data", hotStrings);
        if (CollectionUtils.isNotEmpty(defaultStrings)) {
            r.add("default", defaultStrings.get(0));
        }
        return r;
    }

}
