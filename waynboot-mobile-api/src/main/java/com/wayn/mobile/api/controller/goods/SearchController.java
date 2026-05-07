package com.wayn.mobile.api.controller.goods;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.entity.shop.Keyword;
import com.wayn.common.core.entity.shop.SearchHistory;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IKeywordService;
import com.wayn.common.core.service.shop.ISearchHistoryService;
import com.wayn.common.model.request.SearchRequestVO;
import com.wayn.common.model.response.HotKeywordsResVO;
import com.wayn.common.model.response.SearchGoodsItemResVO;
import com.wayn.data.elastic.constant.EsConstants;
import com.wayn.data.elastic.manager.ElasticDocument;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 搜索接口
 *
 * @author wayn
 * @since 2020-09-23
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("search")
public class SearchController extends BaseController {

    private final IGoodsService iGoodsService;
    private final ISearchHistoryService iSearchHistoryService;
    private final IKeywordService iKeywordService;
    private final ElasticDocument elasticDocument;
    private final ThreadPoolTaskExecutor commonThreadPoolTaskExecutor;

    /**
     * 商城搜索建议
     *
     * @param searchRequestVO 搜索参数
     * @return R
     */
    @GetMapping("sugguest")
    public R<List<String>> sugguest(SearchRequestVO searchRequestVO) throws IOException {
        String keyword = searchRequestVO.getKeyword();
        log.info("查询搜索建议开始, keyword={}", safeKeyword(keyword));
        String suggestField = "name.py";
        String suggestName = "my-suggest";
        SuggestionBuilder<CompletionSuggestionBuilder> termSuggestionBuilder = SuggestBuilders.completionSuggestion(suggestField)
                .prefix(keyword)
                .skipDuplicates(true)
                .size(10);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion(suggestName, termSuggestionBuilder);
        List<String> list = elasticDocument.searchSuggest(EsConstants.ES_GOODS_INDEX, suggestName, suggestBuilder);
        log.info("查询搜索建议完成, keyword={}, count={}", safeKeyword(keyword), list.size());
        return R.success(list);
    }

    /**
     * 商城搜索结果
     *
     * @param searchRequestVO 搜索参数
     * @return R
     */
    @GetMapping("result")
    public R<List<SearchGoodsItemResVO>> result(SearchRequestVO searchRequestVO) throws IOException {
        // 获取筛选、排序条件
        Long memberId = MobileSecurityUtils.getUserId();
        String keyword = searchRequestVO.getKeyword();
        Boolean filterNew = searchRequestVO.getFilterNew();
        Boolean filterHot = searchRequestVO.getFilterHot();
        Boolean isNew = searchRequestVO.getIsNew();
        Boolean isHot = searchRequestVO.getIsHot();
        Boolean isPrice = searchRequestVO.getIsPrice();
        Boolean isSales = searchRequestVO.getIsSales();
        String orderBy = searchRequestVO.getOrderBy();

        Page<SearchRequestVO> page = getPage();
        log.info("查询搜索结果开始, userId={}, keyword={}, pageNum={}, pageSize={}, filterNew={}, filterHot={}, isNew={}, isHot={}, isPrice={}, isSales={}, orderBy={}",
                memberId, safeKeyword(keyword), page.getCurrent(), page.getSize(),
                Boolean.TRUE.equals(filterNew), Boolean.TRUE.equals(filterHot),
                Boolean.TRUE.equals(isNew), Boolean.TRUE.equals(isHot),
                Boolean.TRUE.equals(isPrice), Boolean.TRUE.equals(isSales), orderBy);
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
        if (Boolean.TRUE.equals(isNew)) {
            searchSourceBuilder.sort(new FieldSortBuilder("isNew").order(SortOrder.DESC));
        }
        // 按是否热品排序
        if (Boolean.TRUE.equals(isHot)) {
            searchSourceBuilder.sort(new FieldSortBuilder("isHot").order(SortOrder.DESC));
        }
        // 按价格高低排序
        if (Boolean.TRUE.equals(isPrice)) {
            searchSourceBuilder.sort(new FieldSortBuilder("retailPrice").order("asc".equals(orderBy) ? SortOrder.ASC : SortOrder.DESC));
        }
        // 按销量排序
        if (Boolean.TRUE.equals(isSales)) {
            searchSourceBuilder.sort(new FieldSortBuilder("sales").order(SortOrder.DESC));
        }
        // 筛选新品
        if (Boolean.TRUE.equals(filterNew)) {
            MatchQueryBuilder filterQuery = QueryBuilders.matchQuery("isNew", true);
            boolQueryBuilder.filter(filterQuery);
        }
        // 筛选热品
        if (Boolean.TRUE.equals(filterHot)) {
            MatchQueryBuilder filterQuery = QueryBuilders.matchQuery("isHot", true);
            boolQueryBuilder.filter(filterQuery);
        }

        // 组装Elasticsearch查询条件
        searchSourceBuilder.query(boolQueryBuilder);
        // Elasticsearch分页相关
        searchSourceBuilder.from((int) (page.getCurrent() - 1) * (int) page.getSize());
        searchSourceBuilder.size((int) page.getSize());
        // 执行Elasticsearch查询
        List<JSONObject> list = elasticDocument.searchResult(EsConstants.ES_GOODS_INDEX, searchSourceBuilder, JSONObject.class);
        List<Integer> goodsIdList = list.stream().map(jsonObject -> (Integer) jsonObject.get("id")).collect(Collectors.toList());
        if (goodsIdList.isEmpty()) {
            log.info("查询搜索结果完成, userId={}, keyword={}, count=0", memberId, safeKeyword(keyword));
            return R.success(Collections.emptyList());
        }
        // 根据Elasticsearch中返回商品ID查询商品详情并保持es中的排序
        List<Goods> goodsList = iGoodsService.searchResult(goodsIdList);
        Map<Integer, Goods> goodsMap = goodsList.stream().collect(Collectors.toMap(goods -> Math.toIntExact(goods.getId()), o -> o));
        List<SearchGoodsItemResVO> returnGoodsList = new ArrayList<>(goodsList.size());
        for (Integer goodsId : goodsIdList) {
            Goods goods = goodsMap.get(goodsId);
            if (goods != null) {
                returnGoodsList.add(toSearchGoodsItemResVO(goods));
            }
        }
        if (CollectionUtils.isNotEmpty(goodsList)) {
            commonThreadPoolTaskExecutor.execute(() -> {
                SearchHistory searchHistory = new SearchHistory();
                if (memberId != null && StringUtils.isNotEmpty(keyword)) {
                    searchHistory.setCreateTime(LocalDateTime.now());
                    searchHistory.setUserId(memberId);
                    searchHistory.setKeyword(keyword);
                    searchHistory.setHasGoods(true);
                    iSearchHistoryService.save(searchHistory);
                    log.info("异步保存搜索历史完成, userId={}, keyword={}", memberId, safeKeyword(keyword));
                }
            });
        }
        log.info("查询搜索结果完成, userId={}, keyword={}, count={}", memberId, safeKeyword(keyword), returnGoodsList.size());
        return R.success(returnGoodsList);
    }

    /**
     * 热门搜索词
     *
     * @return R
     */
    @GetMapping("hotKeywords")
    public R<HotKeywordsResVO> hotKeywords() {
        log.info("查询热门搜索词开始");
        // 查询配置了热门搜索展示的关键词
        List<Keyword> hotKeywords = iKeywordService.list(new QueryWrapper<Keyword>().eq("is_hot", true).orderByAsc("sort"));
        List<String> hotStrings = hotKeywords.stream().map(Keyword::getKeyword).collect(Collectors.toList());

        // 查询配置了默认搜索展示的关键词，如果有多个配置了默认搜索，就按照排序值从小到大取第一个
        List<Keyword> defaultKeyword = iKeywordService.list(new QueryWrapper<Keyword>().eq("is_default", true).orderByAsc("sort"));
        List<String> defaultStrings = defaultKeyword.stream().map(Keyword::getKeyword).collect(Collectors.toList());
        HotKeywordsResVO resVO = new HotKeywordsResVO();
        resVO.setHotStrings(hotStrings);
        resVO.setDefaultSearch(defaultStrings.isEmpty() ? null : defaultStrings.get(0));
        log.info("查询热门搜索词完成, hotCount={}, hasDefault={}", hotStrings.size(), !defaultStrings.isEmpty());
        return R.success(resVO);
    }

    private SearchGoodsItemResVO toSearchGoodsItemResVO(Goods goods) {
        SearchGoodsItemResVO resVO = new SearchGoodsItemResVO();
        resVO.setId(goods.getId());
        resVO.setGoodsSn(goods.getGoodsSn());
        resVO.setCategoryId(goods.getCategoryId());
        resVO.setBrandId(goods.getBrandId());
        resVO.setName(goods.getName());
        resVO.setGallery(goods.getGallery());
        resVO.setKeywords(goods.getKeywords());
        resVO.setBrief(goods.getBrief());
        resVO.setIsOnSale(goods.getIsOnSale());
        resVO.setSort(goods.getSort());
        resVO.setPicUrl(goods.getPicUrl());
        resVO.setShareUrl(goods.getShareUrl());
        resVO.setIsNew(goods.getIsNew());
        resVO.setIsHot(goods.getIsHot());
        resVO.setUnit(goods.getUnit());
        resVO.setCounterPrice(goods.getCounterPrice());
        resVO.setRetailPrice(goods.getRetailPrice());
        resVO.setActualSales(goods.getActualSales());
        resVO.setVirtualSales(goods.getVirtualSales());
        resVO.setCreateTime(goods.getCreateTime());
        resVO.setUpdateTime(goods.getUpdateTime());
        return resVO;
    }

    private String safeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        return StringUtils.abbreviate(keyword, 20);
    }

}
