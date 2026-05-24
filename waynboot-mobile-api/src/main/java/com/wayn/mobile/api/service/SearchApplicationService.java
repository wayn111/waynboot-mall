package com.wayn.mobile.api.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.entity.Keyword;
import com.wayn.domain.api.goods.entity.SearchHistory;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.domain.api.goods.service.IKeywordService;
import com.wayn.domain.api.goods.service.ISearchHistoryService;
import com.wayn.common.model.request.SearchRequestVO;
import com.wayn.common.model.response.HotKeywordsResVO;
import com.wayn.common.model.response.SearchGoodsItemResVO;
import com.wayn.data.elastic.constant.EsConstants;
import com.wayn.data.elastic.manager.ElasticDocument;
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 移动端搜索应用服务。
 * 承接搜索 Controller 下沉的 ES 查询、DB 回查、热门词查询和搜索历史异步写入逻辑，保持入口层轻量。
 */
@Slf4j
@Service
@AllArgsConstructor
public class SearchApplicationService {

    private static final String SUGGEST_FIELD = "name.py";
    private static final String SUGGEST_NAME = "my-suggest";
    private static final int SUGGEST_SIZE = 10;

    private final IGoodsService goodsService;
    private final ISearchHistoryService searchHistoryService;
    private final IKeywordService keywordService;
    private final ElasticDocument elasticDocument;
    private final ThreadPoolTaskExecutor commonThreadPoolTaskExecutor;

    /**
     * 查询商城搜索建议。
     *
     * @param searchRequestVO 搜索参数
     * @return 搜索建议列表
     * @throws IOException ES 查询异常
     */
    public List<String> suggest(SearchRequestVO searchRequestVO) throws IOException {
        String keyword = searchRequestVO.getKeyword();
        SuggestionBuilder<CompletionSuggestionBuilder> termSuggestionBuilder = SuggestBuilders
                .completionSuggestion(SUGGEST_FIELD)
                .prefix(keyword)
                .skipDuplicates(true)
                .size(SUGGEST_SIZE);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion(SUGGEST_NAME, termSuggestionBuilder);
        return elasticDocument.searchSuggest(EsConstants.ES_GOODS_INDEX, SUGGEST_NAME, suggestBuilder);
    }

    /**
     * 查询商城搜索结果。
     * ES 负责召回和排序，DB 负责返回最新商品展示字段，最终按 ES 返回的商品 ID 顺序组装 VO。
     *
     * @param searchRequestVO 搜索参数
     * @param page 分页参数
     * @param memberId 当前会员 ID
     * @return 商品搜索结果 VO 列表
     * @throws IOException ES 查询异常
     */
    public List<SearchGoodsItemResVO> searchResult(SearchRequestVO searchRequestVO, Page<SearchRequestVO> page,
                                                   Long memberId) throws IOException {
        SearchSourceBuilder searchSourceBuilder = buildSearchSource(searchRequestVO, page);
        List<JSONObject> list = elasticDocument.searchResult(EsConstants.ES_GOODS_INDEX, searchSourceBuilder, JSONObject.class);
        List<Integer> goodsIdList = list.stream().map(jsonObject -> (Integer) jsonObject.get("id")).collect(Collectors.toList());
        if (goodsIdList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Goods> goodsList = goodsService.searchResult(goodsIdList);
        List<SearchGoodsItemResVO> result = buildResultInEsOrder(goodsIdList, goodsList);
        saveSearchHistoryAsync(memberId, searchRequestVO.getKeyword(), goodsList);
        return result;
    }

    /**
     * 查询热门搜索词和默认搜索词。
     *
     * @return 热门搜索词响应 VO
     */
    public HotKeywordsResVO hotKeywords() {
        List<Keyword> hotKeywords = keywordService.list(new QueryWrapper<Keyword>().eq("is_hot", true).orderByAsc("sort"));
        List<String> hotStrings = hotKeywords.stream().map(Keyword::getKeyword).collect(Collectors.toList());
        List<Keyword> defaultKeyword = keywordService.list(new QueryWrapper<Keyword>().eq("is_default", true).orderByAsc("sort"));
        List<String> defaultStrings = defaultKeyword.stream().map(Keyword::getKeyword).collect(Collectors.toList());
        HotKeywordsResVO resVO = new HotKeywordsResVO();
        resVO.setHotStrings(hotStrings);
        resVO.setDefaultSearch(defaultStrings.isEmpty() ? null : defaultStrings.get(0));
        return resVO;
    }

    /**
     * 构建 ES 搜索条件。
     *
     * @param searchRequestVO 搜索参数
     * @param page 分页参数
     * @return ES 搜索条件
     */
    private SearchSourceBuilder buildSearchSource(SearchRequestVO searchRequestVO, Page<SearchRequestVO> page) {
        String keyword = searchRequestVO.getKeyword();
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
        applySort(searchRequestVO, searchSourceBuilder);
        applyFilter(searchRequestVO, boolQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from((int) (page.getCurrent() - 1) * (int) page.getSize());
        searchSourceBuilder.size((int) page.getSize());
        return searchSourceBuilder;
    }

    /**
     * 应用搜索排序条件。
     *
     * @param searchRequestVO 搜索参数
     * @param searchSourceBuilder ES 搜索条件
     */
    private void applySort(SearchRequestVO searchRequestVO, SearchSourceBuilder searchSourceBuilder) {
        if (Boolean.TRUE.equals(searchRequestVO.getIsNew())) {
            searchSourceBuilder.sort(new FieldSortBuilder("isNew").order(SortOrder.DESC));
        }
        if (Boolean.TRUE.equals(searchRequestVO.getIsHot())) {
            searchSourceBuilder.sort(new FieldSortBuilder("isHot").order(SortOrder.DESC));
        }
        if (Boolean.TRUE.equals(searchRequestVO.getIsPrice())) {
            SortOrder sortOrder = "asc".equals(searchRequestVO.getOrderBy()) ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(new FieldSortBuilder("retailPrice").order(sortOrder));
        }
        if (Boolean.TRUE.equals(searchRequestVO.getIsSales())) {
            searchSourceBuilder.sort(new FieldSortBuilder("sales").order(SortOrder.DESC));
        }
    }

    /**
     * 应用搜索过滤条件。
     *
     * @param searchRequestVO 搜索参数
     * @param boolQueryBuilder ES bool 查询条件
     */
    private void applyFilter(SearchRequestVO searchRequestVO, BoolQueryBuilder boolQueryBuilder) {
        if (Boolean.TRUE.equals(searchRequestVO.getFilterNew())) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery("isNew", true));
        }
        if (Boolean.TRUE.equals(searchRequestVO.getFilterHot())) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery("isHot", true));
        }
    }

    /**
     * 按 ES 返回顺序组装商品结果。
     *
     * @param goodsIdList ES 返回的商品 ID 顺序
     * @param goodsList DB 查询到的商品列表
     * @return 商品搜索结果 VO 列表
     */
    private List<SearchGoodsItemResVO> buildResultInEsOrder(List<Integer> goodsIdList, List<Goods> goodsList) {
        Map<Integer, Goods> goodsMap = goodsList.stream().collect(Collectors.toMap(goods -> Math.toIntExact(goods.getId()), o -> o));
        List<SearchGoodsItemResVO> returnGoodsList = new ArrayList<>(goodsList.size());
        for (Integer goodsId : goodsIdList) {
            Goods goods = goodsMap.get(goodsId);
            if (goods != null) {
                returnGoodsList.add(toSearchGoodsItemResVO(goods));
            }
        }
        return returnGoodsList;
    }

    /**
     * 异步保存搜索历史。
     *
     * @param memberId 当前会员 ID
     * @param keyword 搜索关键词
     * @param goodsList DB 查询到的商品列表
     */
    private void saveSearchHistoryAsync(Long memberId, String keyword, List<Goods> goodsList) {
        if (CollectionUtils.isEmpty(goodsList)) {
            return;
        }
        commonThreadPoolTaskExecutor.execute(() -> {
            if (memberId == null || StringUtils.isEmpty(keyword)) {
                return;
            }
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setCreateTime(LocalDateTime.now());
            searchHistory.setUserId(memberId);
            searchHistory.setKeyword(keyword);
            searchHistory.setHasGoods(true);
            searchHistoryService.save(searchHistory);
            log.info("异步保存搜索历史完成, userId={}, keyword={}", memberId, safeKeyword(keyword));
        });
    }

    /**
     * 转换商品实体为搜索结果 VO。
     *
     * @param goods 商品实体
     * @return 搜索结果 VO
     */
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

    /**
     * 缩短日志里的搜索词。
     *
     * @param keyword 搜索词
     * @return 脱敏后的搜索词
     */
    private String safeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        return StringUtils.abbreviate(keyword, 20);
    }
}
