package com.wayn.admin.api.controller.shop;


import com.wayn.admin.framework.manager.elastic.service.BaseElasticService;
import com.wayn.common.base.ElasticEntity;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
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
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "沃尔玛"));
        List<Object> list = baseElasticService.search("goods", searchSourceBuilder, Object.class);
        return R.success().add("data", list);
    }
}
