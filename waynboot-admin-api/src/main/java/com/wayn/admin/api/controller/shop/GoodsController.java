package com.wayn.admin.api.controller.shop;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.constant.SysConstants;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.vo.GoodsSaveRelatedVO;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import com.wayn.common.util.file.FileUtils;
import com.wayn.data.elastic.manager.ElasticDocument;
import com.wayn.data.elastic.manager.ElasticEntity;
import com.wayn.data.redis.manager.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    private ElasticDocument elasticDocument;
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
    public R updateGoods(@Validated @RequestBody GoodsSaveRelatedVO goodsSaveRelatedVO) throws IOException {
        return iGoodsService.updateGoodsRelated(goodsSaveRelatedVO);
    }

    @GetMapping("{goodsId}")
    public R getGoods(@PathVariable Long goodsId) {
        return R.success().add("data", iGoodsService.getGoodsInfoById(goodsId));
    }

    @DeleteMapping("{goodsId}")
    public R deleteGoods(@PathVariable Long goodsId) throws IOException {
        return R.result(iGoodsService.deleteGoodsRelatedByGoodsId(goodsId));
    }

    @PostMapping("syncEs")
    public R syncEs() {
        if (redisCache.getCacheObject(SysConstants.ES_GOODS_INDEX_KEY) != null) {
            return R.error(ReturnCodeEnum.CUSTOM_ERROR.setMsg("正在同步，请稍等"));
        }
        boolean flag = false;
        redisCache.setCacheObject(SysConstants.ES_GOODS_INDEX_KEY, true, 3, TimeUnit.MINUTES);
        try {
            elasticDocument.deleteIndex(SysConstants.ES_GOODS_INDEX);
            InputStream inputStream = this.getClass().getResourceAsStream(SysConstants.ES_INDEX_GOODS_FILENAME);
            if (elasticDocument.createIndex(SysConstants.ES_GOODS_INDEX, FileUtils.getContent(inputStream))) {
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
                flag = elasticDocument.insertBatch("goods", entities);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            redisCache.deleteObject(SysConstants.ES_GOODS_INDEX_KEY);
        }
        return R.result(flag);
    }
}
