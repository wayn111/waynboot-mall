package com.wayn.admin.api.controller.shop;


import com.wayn.admin.framework.manager.elastic.service.BaseElasticService;
import com.wayn.common.base.ElasticEntity;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("elastic")
public class ElasticController {

    @Autowired
    private BaseElasticService baseElasticService;

    @Autowired
    private IGoodsService iGoodsService;


    @GetMapping("index")
    public R index() {
        String indexSql = "{\n" +
                "    \"properties\": {\n" +
                "            \"name\": {\n" +
                "                \"type\": \"text\",\n" +
                "                \"analyzer\": \"ik_max_word\"\n" +
                "            },\n" +
                "            \"keyword\": {\n" +
                "                \"type\": \"keyword\"\n" +
                "            },\n" +
                "            \"isOnSale\": {\n" +
                "                \"type\": \"boolean\"\n" +
                "            }\n" +
                "        }\n" +
                "}";
        baseElasticService.createIndex("goods", indexSql);
        return R.success();
    }

    @GetMapping("insert")
    public R insert() {
        List<Goods> list = iGoodsService.list();
        List<ElasticEntity> entities = new ArrayList<>();
        for (Goods goods : list) {
            ElasticEntity elasticEntity = new ElasticEntity();
            Map<String, Object> map = new HashMap<>();
            elasticEntity.setId(goods.getId().toString());
            map.put("name", goods.getName());
            map.put("keyword", goods.getKeywords());
            map.put("isOnSale", goods.getIsOnSale());
            elasticEntity.setData(map);
            entities.add(elasticEntity);
        }
        baseElasticService.insertBatch("goods", entities);
        return R.success();
    }

    @GetMapping("search")
    public R search() {
//        MultiSearchRequest request = new MultiSearchRequest();
//        SearchRequest firstSearchRequest = new SearchRequest("goods");
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "羊毛"));
//        firstSearchRequest.source(searchSourceBuilder);
//        request.add(firstSearchRequest);
//        SearchRequest secondSearchRequest = new SearchRequest("goods");
//        searchSourceBuilder = new SearchSourceBuilder();
//
//        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "手机"));
//        secondSearchRequest.source(searchSourceBuilder);
//        request.add(firstSearchRequest);
//        request.add(secondSearchRequest);
//        List<Object> list = baseElasticService.search(request, Object.class);
        // 查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("name", "手机");
        MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery("keyword", "手机");
        boolQueryBuilder.should(matchQuery);
        boolQueryBuilder.should(matchPhraseQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        List<Object> list = baseElasticService.search("goods", searchSourceBuilder, Object.class);
        return R.success().add("data", list);
    }
}
