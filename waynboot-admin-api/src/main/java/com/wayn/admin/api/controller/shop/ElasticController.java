package com.wayn.admin.api.controller.shop;


import com.wayn.admin.framework.manager.elastic.service.BaseElasticService;
import com.wayn.common.base.ElasticEntity;
import com.wayn.common.util.R;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("elastic")
public class ElasticController {

    @Autowired
    private BaseElasticService baseElasticService;

    @GetMapping("insert")
    public R insert() {
        ElasticEntity elasticEntity = new ElasticEntity();
        elasticEntity.setId("2");
        Map<String, Object> map = new HashMap<>();
        map.put("name", "书本2");
        map.put("price", 1000D);
        map.put("date", new Date());
        elasticEntity.setData(map);
        baseElasticService.insertOrUpdateOne("store", elasticEntity);
        return R.success();
    }

    @GetMapping("search")
    public R search() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "沃尔玛"));
        List<Object> list = baseElasticService.search("store", searchSourceBuilder, Object.class);
        return R.success().add("data", list);
    }
}
