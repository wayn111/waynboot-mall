package com.wayn.admin.api.controller.shop;


import com.alibaba.fastjson.JSONObject;
import com.wayn.common.base.entity.ElasticEntity;
import com.wayn.common.base.service.BaseElasticService;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
import com.wayn.common.util.file.FileUtils;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("elastic")
public class ElasticController {

    @Autowired
    private BaseElasticService baseElasticService;

    @Autowired
    private IGoodsService iGoodsService;


    @GetMapping("index")
    public R index() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/es/index/goods");
        System.out.println(FileUtils.getContent(is));
//        baseElasticService.createIndex("goods", indexSql);
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
            map.put("id", goods.getId());
            map.put("name", goods.getName());
            map.put("countPrice", goods.getCounterPrice());
            map.put("retailPrice", goods.getRetailPrice());
            map.put("keyword", goods.getKeywords());
            map.put("isOnSale", goods.getIsOnSale());
            elasticEntity.setData(map);
            entities.add(elasticEntity);
        }
        baseElasticService.insertBatch("goods", entities);
        return R.success();
    }

    @GetMapping("search")
    public R search(String keyword, int page, int pageSize) {
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
        MatchQueryBuilder matchQuery1 = QueryBuilders.matchQuery("name", keyword);
//        MatchQueryBuilder matchQuery2 = QueryBuilders.matchQuery("isOnSale", true);
        MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery("keyword", keyword);
        boolQueryBuilder.should(matchQuery1).should(matchPhraseQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(page);
        searchSourceBuilder.size(pageSize);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        List<JSONObject> list = baseElasticService.search("goods", searchSourceBuilder, JSONObject.class);
        list = list.stream().filter(jsonObject -> (boolean) jsonObject.get("isOnSale")).collect(Collectors.toList());
        return R.success().add("data", list);
    }
}
