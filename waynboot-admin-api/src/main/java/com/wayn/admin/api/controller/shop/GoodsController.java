package com.wayn.admin.api.controller.shop;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.framework.manager.elastic.service.BaseElasticService;
import com.wayn.common.base.BaseController;
import com.wayn.common.base.ElasticEntity;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.vo.GoodsSaveRelatedVO;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
import com.wayn.common.util.file.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品基本信息表 前端控制器
 * </p>
 *
 * @author wayn
 * @since 2020-07-06
 */
@RestController
@RequestMapping("/shop/goods")
public class GoodsController extends BaseController {

    @Autowired
    private IGoodsService iGoodsService;

    @Autowired
    private BaseElasticService baseElasticService;

    @GetMapping("/list")
    public R list(Goods goods) {
        Page<Goods> page = getPage();
        return R.success().add("page", iGoodsService.listPage(page, goods));
    }

    @PostMapping
    public R addGoods(@Validated @RequestBody GoodsSaveRelatedVO goodsSaveRelatedVO) {
        return iGoodsService.saveGoodsRelated(goodsSaveRelatedVO);
    }

    @PutMapping
    public R updateGoods(@Validated @RequestBody GoodsSaveRelatedVO goodsSaveRelatedVO) {
        return iGoodsService.updateGoodsRelated(goodsSaveRelatedVO);
    }

    @GetMapping("{goodsId}")
    public R getGoods(@PathVariable Long goodsId) {
        return R.success().add("data", iGoodsService.getGoodsInfoById(goodsId));
    }

    @DeleteMapping("{goodsId}")
    public R deleteGoods(@PathVariable Long goodsId) {
        return R.result(iGoodsService.deleteGoodsRelatedByGoodsId(goodsId));
    }

    @PostMapping("syncEs")
    public R syncEs() {
        baseElasticService.deleteIndex("goods");
        baseElasticService.createIndex("goos", FileUtils.getContent(this.getClass().getResourceAsStream(SysConstants.ES_INDEX_GOODS_FILENAME)));
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
        return R.result(baseElasticService.insertBatch("goods", entities));
    }
}
