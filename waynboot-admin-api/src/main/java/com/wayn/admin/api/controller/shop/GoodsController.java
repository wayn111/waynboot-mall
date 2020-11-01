package com.wayn.admin.api.controller.shop;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.framework.redis.RedisCache;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.base.entity.ElasticEntity;
import com.wayn.common.base.service.BaseElasticService;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.vo.GoodsSaveRelatedVO;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
import com.wayn.common.util.file.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 商品基本信息表 前端控制器
 * </p>
 *
 * @author wayn
 * @since 2020-07-06
 */
@Slf4j
@RestController
@RequestMapping("/shop/goods")
public class GoodsController extends BaseController {

    @Autowired
    private IGoodsService iGoodsService;
    @Autowired
    private BaseElasticService baseElasticService;
    @Autowired
    private RedisCache redisCache;

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
        if (redisCache.getCacheObject(SysConstants.REDIS_ES_GOODS_INDEX) != null) {
            return R.error("正在同步，请稍等");
        }
        boolean flag = false;
        redisCache.setCacheObject(SysConstants.REDIS_ES_GOODS_INDEX, true, 3, TimeUnit.MINUTES);
        try {
            baseElasticService.deleteIndex(SysConstants.ES_GOODS_INDEX);
            InputStream inputStream = this.getClass().getResourceAsStream(SysConstants.ES_INDEX_GOODS_FILENAME);
            if (baseElasticService.createIndex(SysConstants.ES_GOODS_INDEX, FileUtils.getContent(inputStream))) {
                List<Goods> list = iGoodsService.list();
                List<ElasticEntity> entities = new ArrayList<>();
                for (Goods goods : list) {
                    ElasticEntity elasticEntity = new ElasticEntity();
                    Map<String, Object> map = new HashMap<>();
                    elasticEntity.setId(goods.getId().toString());
                    map.put("id", goods.getId());
                    map.put("name", goods.getName());
                    map.put("sales", goods.getActualSales() + goods.getVirtualSales());
                    map.put("isHot", goods.getIsHot());
                    map.put("isNew", goods.getIsNew());
                    map.put("countPrice", goods.getCounterPrice());
                    map.put("retailPrice", goods.getRetailPrice());
                    map.put("keyword", goods.getKeywords().split(","));
                    map.put("isOnSale", goods.getIsOnSale());
                    map.put("createTime", goods.getCreateTime());
                    elasticEntity.setData(map);
                    entities.add(elasticEntity);
                }
                flag = baseElasticService.insertBatch("goods", entities);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            redisCache.deleteObject(SysConstants.REDIS_ES_GOODS_INDEX);
        }
        return R.result(flag);
    }
}
